package org.easycluster.easycluster.http;

import static org.junit.Assert.assertEquals;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.httpclient.HttpVersion;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.RequestEntity;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.httpclient.protocol.Protocol;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.cluster.ssl.SSLConfig;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class HttpClientTestCase {

	@Test
	public void testOpenUrl() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(8080);

		SerializationConfig decodeSerializeConfig = new SerializationConfig();
		decodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		decodeSerializeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeSerializeConfig);

		SerializationConfig encodeSerializeConfig = new SerializationConfig();
		encodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		encodeSerializeConfig.setSerializeType(SerializeType.JSON);

		serverConfig.setEncodeSerializeConfig(encodeSerializeConfig);

		HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		String href = "http://127.0.0.1:8080/289?intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1";

		BufferedReader br = null;
		try {
			URL url = new URL(href);
			br = new BufferedReader(new InputStreamReader(url.openStream(), "UTF-8"));

			StringBuffer sb = new StringBuffer();
			String line = "";
			while ((line = br.readLine()) != null) {
				sb.append(line);
			}

			System.out.println(JSON.parse(sb.toString()));
		} finally {
			if (br != null) {
				br.close();
			}
			server.stop();
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testPost_https() throws InterruptedException, HttpException, IOException {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(8080);

		SSLConfig sslConfig = new SSLConfig();
		sslConfig.setKeyStore("/Users/wangqi/.serverkeystore");
		sslConfig.setKeyStorePassword("123456");
		serverConfig.setSslConfig(sslConfig);

		SerializationConfig decodeSerializeConfig = new SerializationConfig();
		decodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		decodeSerializeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeSerializeConfig);

		SerializationConfig encodeSerializeConfig = new SerializationConfig();
		encodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		encodeSerializeConfig.setSerializeType(SerializeType.JSON);

		serverConfig.setEncodeSerializeConfig(encodeSerializeConfig);

		HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		try {
			String url = "https://127.0.0.1:8080/289";

			PostMethod method = new PostMethod(url);
			RequestEntity requestEntity = new StringRequestEntity(
					"intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1", "", "UTF-8");
			method.setRequestEntity(requestEntity);

			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			httpClient.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "test-client");

			// Allow access even though certificate is self signed
			Protocol lEasyHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8080);
			Protocol.registerProtocol("https", lEasyHttps);

			int status = httpClient.executeMethod(method);

			assertEquals(HttpStatus.SC_OK, status);

			byte[] responseBody = method.getResponseBody();
			System.out.println(JSON.parse(new String(responseBody)));

		} finally {
			server.stop();
		}
	}

	@Test
	public void testPost() throws InterruptedException, HttpException, IOException {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(8080);

		SerializationConfig decodeSerializeConfig = new SerializationConfig();
		decodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		decodeSerializeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeSerializeConfig);

		SerializationConfig encodeSerializeConfig = new SerializationConfig();
		encodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		encodeSerializeConfig.setSerializeType(SerializeType.JSON);

		serverConfig.setEncodeSerializeConfig(encodeSerializeConfig);

		HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		try {
			String url = "http://127.0.0.1:8080/289";
			PostMethod method = new PostMethod(url);
			RequestEntity requestEntity = new StringRequestEntity(
					"intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1", "", "UTF-8");
			method.setRequestEntity(requestEntity);

			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			httpClient.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "test-client");

			int status = httpClient.executeMethod(method);

			assertEquals(HttpStatus.SC_OK, status);

			byte[] responseBody = method.getResponseBody();
			System.out.println(JSON.parse(new String(responseBody)));
		} finally {
			server.stop();
		}
	}

	@Test
	public void testGet() throws InterruptedException, HttpException, IOException {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(8080);

		SerializationConfig decodeSerializeConfig = new SerializationConfig();
		decodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		decodeSerializeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeSerializeConfig);

		SerializationConfig encodeSerializeConfig = new SerializationConfig();
		encodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		encodeSerializeConfig.setSerializeType(SerializeType.JSON);

		serverConfig.setEncodeSerializeConfig(encodeSerializeConfig);

		HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		try {
			String url = "http://127.0.0.1:8080/289?intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1";
			GetMethod method = new GetMethod(url);

			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			httpClient.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "test-client");

			int status = httpClient.executeMethod(method);

			assertEquals(HttpStatus.SC_OK, status);

			byte[] responseBody = method.getResponseBody();
			System.out.println(JSON.parse(new String(responseBody)));
		} finally {
			server.stop();
		}
	}

	@SuppressWarnings("deprecation")
	@Test
	public void testGet_https() throws InterruptedException, HttpException, IOException {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(8080);

		SSLConfig sslConfig = new SSLConfig();
		sslConfig.setKeyStore("/Users/wangqi/.serverkeystore");
		sslConfig.setKeyStorePassword("123456");
		serverConfig.setSslConfig(sslConfig);

		SerializationConfig decodeSerializeConfig = new SerializationConfig();
		decodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		decodeSerializeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeSerializeConfig);

		SerializationConfig encodeSerializeConfig = new SerializationConfig();
		encodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		encodeSerializeConfig.setSerializeType(SerializeType.JSON);

		serverConfig.setEncodeSerializeConfig(encodeSerializeConfig);

		HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		try {
			String url = "https://127.0.0.1:8080/289?intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1";
			GetMethod method = new GetMethod(url);

			HttpClient httpClient = new HttpClient();
			httpClient.getParams().setParameter(HttpMethodParams.RETRY_HANDLER, new DefaultHttpMethodRetryHandler());
			httpClient.getParams().setParameter(HttpMethodParams.PROTOCOL_VERSION, HttpVersion.HTTP_1_1);
			httpClient.getParams().setParameter(HttpMethodParams.HTTP_CONTENT_CHARSET, "UTF-8");
			httpClient.getParams().setParameter(HttpMethodParams.SO_TIMEOUT, 5000);
			httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT, "test-client");

			// Allow access even though certificate is self signed
			Protocol lEasyHttps = new Protocol("https", new EasySSLProtocolSocketFactory(), 8080);
			Protocol.registerProtocol("https", lEasyHttps);

			int status = httpClient.executeMethod(method);

			assertEquals(HttpStatus.SC_OK, status);

			byte[] responseBody = method.getResponseBody();
			System.out.println(JSON.parse(new String(responseBody)));

		} finally {
			server.stop();
		}
	}

}
