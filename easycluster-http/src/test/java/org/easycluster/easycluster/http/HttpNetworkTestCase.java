package org.easycluster.easycluster.http;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.cluster.ssl.SSLConfig;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class HttpNetworkTestCase {

	private HttpServer	server;
	private HttpClient	client;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
		if (client != null) {
			client.stop();
		}
		if (server != null) {
			server.stop();
		}
	}

	@Test
	public void testHttps_simpleway() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

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

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SSLConfig clientSslConfig = new SSLConfig();
		clientConfig.setSslConfig(clientSslConfig);

		SerializationConfig clientDecodeSerializeConfig = new SerializationConfig();
		clientDecodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		clientDecodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		clientDecodeSerializeConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setDecodeSerializeConfig(clientDecodeSerializeConfig);

		SerializationConfig clientEncodeSerializeConfig = new SerializationConfig();
		clientEncodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		clientEncodeSerializeConfig.setSerializeType(SerializeType.KV);

		clientConfig.setEncodeSerializeConfig(clientEncodeSerializeConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testHttps_twoway() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

		SSLConfig sslConfig = new SSLConfig();
		sslConfig.setKeyStore("/Users/wangqi/.serverkeystore");
		sslConfig.setKeyStorePassword("123456");
		sslConfig.setTrustStore("/Users/wangqi/.servertruststore");
		sslConfig.setTrustStorePassword("123456");
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

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SSLConfig clientSslConfig = new SSLConfig();
		clientSslConfig.setKeyStore("/Users/wangqi/.clientkeystore");
		clientSslConfig.setKeyStorePassword("123456");
		clientSslConfig.setKeyStore("/Users/wangqi/.clienttruststore");
		clientSslConfig.setKeyStorePassword("123456");
		clientConfig.setSslConfig(clientSslConfig);

		SerializationConfig clientDecodeSerializeConfig = new SerializationConfig();
		clientDecodeSerializeConfig.setTypeMetaInfo(typeMetaInfo);
		clientDecodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		clientDecodeSerializeConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setDecodeSerializeConfig(clientDecodeSerializeConfig);

		SerializationConfig clientEncodeSerializeConfig = new SerializationConfig();
		clientEncodeSerializeConfig.setSerializeBytesDebugEnabled(true);
		clientEncodeSerializeConfig.setSerializeType(SerializeType.KV);

		clientConfig.setEncodeSerializeConfig(clientEncodeSerializeConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testOpenUrl_success() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

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

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		String href = "http://127.0.0.1:6000/289?intField=1234&byteField=1&stringField=22222";

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
		}
	}

	@Test(expected = IOException.class)
	public void testOpenUrl_nullField() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

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

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		String href = "http://127.0.0.1:6000/289?byteField=1&stringField=22222";

		URL url = new URL(href);
		url.openStream();

	}

	@Test
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

	}

	@Test
	public void testSend_batchBinary() {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);

		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		int num = 5000;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(UUID.randomUUID().getMostSignificantBits());
			request.setByteArrayField(new byte[] { 127 });

			client1Requests.add(request);
		}

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(client.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			try {
				Object object = futures.get(i).get(60, TimeUnit.SECONDS);
				client1Responses.add((SampleResponse) object);
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchjson() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);
		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		clientConfig.setWriteTimeoutMillis(600000);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		int num = 500;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(UUID.randomUUID().getMostSignificantBits());
			request.setByteArrayField(new byte[] { 127 });

			client1Requests.add(request);
		}

		final AtomicInteger count = new AtomicInteger();
		server.registerHandler(SampleRequest.class, SampleResponse.class, new MessageClosure<SampleRequest, SampleResponse>() {

			@Override
			public SampleResponse execute(SampleRequest input) {
				System.out.println("received : " + count.incrementAndGet());
				SampleResponse response = new SampleResponse();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(client.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((SampleResponse) futures.get(i).get(60, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());
		Assert.assertEquals(num, count.get());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_json() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		long startTime = System.nanoTime();

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_tlv() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.TLV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		long startTime = System.nanoTime();

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_kv() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.KV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		long startTime = System.nanoTime();

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testKvInJsonOut() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig encodeConfig = new SerializationConfig();
		encodeConfig.setTypeMetaInfo(typeMetaInfo);
		encodeConfig.setSerializeBytesDebugEnabled(true);
		encodeConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(encodeConfig);
		SerializationConfig decodeConfig = new SerializationConfig();
		decodeConfig.setTypeMetaInfo(typeMetaInfo);
		decodeConfig.setSerializeBytesDebugEnabled(true);
		decodeConfig.setSerializeType(SerializeType.KV);
		serverConfig.setDecodeSerializeConfig(decodeConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientEncodeConfig = new SerializationConfig();
		clientEncodeConfig.setTypeMetaInfo(typeMetaInfo);
		clientEncodeConfig.setSerializeBytesDebugEnabled(true);
		clientEncodeConfig.setSerializeType(SerializeType.KV);
		clientConfig.setEncodeSerializeConfig(clientEncodeConfig);
		SerializationConfig clientDecodeConfig = new SerializationConfig();
		clientDecodeConfig.setTypeMetaInfo(typeMetaInfo);
		clientDecodeConfig.setSerializeBytesDebugEnabled(true);
		clientDecodeConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setDecodeSerializeConfig(clientDecodeConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		long startTime = System.nanoTime();

		Future<Object> future = client.sendMessage(request);

		SampleResponse assertobj = (SampleResponse) future.get(60, TimeUnit.SECONDS);
		Assert.assertEquals(request.getIntField(), assertobj.getIntField());
		Assert.assertEquals(request.getShortField(), assertobj.getShortField());
		Assert.assertEquals(request.getLongField(), assertobj.getLongField());
		Assert.assertEquals(request.getByteField(), assertobj.getByteField());
		Assert.assertEquals(request.getStringField(), assertobj.getStringField());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchtlv() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.TLV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.TLV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		int num = 500;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(UUID.randomUUID().getMostSignificantBits());
			request.setByteArrayField(new byte[] { 127 });

			client1Requests.add(request);
		}

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(client.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			try {
				Object object = futures.get(i).get(60, TimeUnit.SECONDS);
				if (object instanceof SampleResponse) {
					client1Responses.add((SampleResponse) object);
				} else {
					System.out.println(object);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test
	public void testSend_batchkv() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeType(SerializeType.KV);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setWriteTimeoutMillis(10000);
		clientConfig.setStaleRequestTimeoutMins(30);

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeType(SerializeType.KV);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		int num = 500;

		List<SampleRequest> client1Requests = new ArrayList<SampleRequest>();

		for (int i = 0; i < num; i++) {
			SampleRequest request = new SampleRequest();
			request.setIntField(1);
			request.setShortField((byte) 1);
			request.setByteField((byte) 1);
			request.setLongField(1L);
			request.setStringField("test");
			request.setClient(UUID.randomUUID().getMostSignificantBits());
			request.setByteArrayField(new byte[] { 127 });

			client1Requests.add(request);
		}

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(client.sendMessage(client1Requests.get(i)));
		}

		final List<SampleResponse> client1Responses = new ArrayList<SampleResponse>();
		for (int i = 0; i < num; i++) {
			try {
				Object object = futures.get(i).get(60, TimeUnit.SECONDS);
				if (object instanceof SampleResponse) {
					client1Responses.add((SampleResponse) object);
				} else {
					System.out.println(object);
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			} catch (TimeoutException e) {
				e.printStackTrace();
			}
		}
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

	}

	@Test(expected = TimeoutException.class)
	public void testSend_blackList() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.http");
		Int2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setServiceGroup("app");
		serverConfig.setService("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		SerializationConfig codecConfig = new SerializationConfig();
		codecConfig.setTypeMetaInfo(typeMetaInfo);
		codecConfig.setSerializeBytesDebugEnabled(true);
		codecConfig.setSerializeType(SerializeType.JSON);
		serverConfig.setEncodeSerializeConfig(codecConfig);
		serverConfig.setDecodeSerializeConfig(codecConfig);

		serverConfig.setBlacklist("10.68.147.33,127.0.0.1");
		
		server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		SerializationConfig clientCodecConfig = new SerializationConfig();
		clientCodecConfig.setTypeMetaInfo(typeMetaInfo);
		clientCodecConfig.setSerializeBytesDebugEnabled(true);
		clientCodecConfig.setSerializeType(SerializeType.JSON);
		clientConfig.setEncodeSerializeConfig(clientCodecConfig);
		clientConfig.setDecodeSerializeConfig(clientCodecConfig);

		client = new HttpClient(clientConfig, new RoundRobinLoadBalancerFactory());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = client.sendMessage(request);

		future.get(1, TimeUnit.SECONDS);

	}
}
