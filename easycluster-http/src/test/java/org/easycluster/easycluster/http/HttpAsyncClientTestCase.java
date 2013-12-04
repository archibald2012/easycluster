package org.easycluster.easycluster.http;

import static org.junit.Assert.fail;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpVersion;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.nio.client.DefaultHttpAsyncClient;
import org.apache.http.nio.client.HttpAsyncClient;
import org.apache.http.nio.concurrent.FutureCallback;
import org.apache.http.nio.entity.NStringEntity;
import org.apache.http.nio.reactor.IOReactorException;
import org.apache.http.params.HttpConnectionParams;
import org.apache.http.params.HttpParams;
import org.apache.http.params.HttpProtocolParams;
import org.apache.http.params.SyncBasicHttpParams;
import org.apache.http.protocol.HTTP;
import org.apache.http.util.EntityUtils;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.alibaba.fastjson.JSON;

public class HttpAsyncClientTestCase {

	private HttpAsyncClient	httpAsyncClient;

	@Before
	public void setUp() throws IOReactorException {

		String userAgent = "test-client";
		int socketTimeout = 5000;
		int connectionTimeout = 10000;

		HttpParams params = new SyncBasicHttpParams();

		HttpProtocolParams.setVersion(params, HttpVersion.HTTP_1_1);
		HttpProtocolParams.setContentCharset(params, HTTP.DEFAULT_CONTENT_CHARSET);
		HttpConnectionParams.setTcpNoDelay(params, true);
		HttpConnectionParams.setSocketBufferSize(params, 8192);
		HttpConnectionParams.setSoTimeout(params, socketTimeout);
		HttpConnectionParams.setConnectionTimeout(params, connectionTimeout);
		HttpProtocolParams.setUserAgent(params, userAgent);

		httpAsyncClient = new DefaultHttpAsyncClient(params);
		httpAsyncClient.start();
	}

	@After
	public void tearDown() throws InterruptedException {
		httpAsyncClient.shutdown();
	}

	@Test
	public void testPost() throws UnsupportedEncodingException, InterruptedException {

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

		final HttpServer server = new HttpServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		String url = "http://127.0.0.1:8080/289";

		HttpPost httpPost = new HttpPost(url);
		HttpEntity httpEntity = new NStringEntity(
				"intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1", "utf-8");

		httpPost.setEntity(httpEntity);

		try {
			final CountDownLatch latch = new CountDownLatch(1);

			httpAsyncClient.execute(httpPost, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {

					try {
						byte[] content = EntityUtils.toByteArray(response.getEntity());
						System.out.println(JSON.parse(new String(content)));
					} catch (Exception e) {
						e.printStackTrace();
					}
					latch.countDown();

				}

				@Override
				public void failed(final Exception ex) {
					fail(ex.getMessage());

				}

				@Override
				public void cancelled() {
				}

			});

			latch.await();
		} finally {
			server.stop();
		}
	}

	@Test
	public void testGet() throws UnsupportedEncodingException, InterruptedException {

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

		String url = "http://127.0.0.1:8080/289?intField=1&shortField=1&longField=1&byteArrayField=127&nanoTime=1385779140881579000&stringField=test&byteField=1";
		HttpGet httpGet = new HttpGet(url);

		try {
			final CountDownLatch latch = new CountDownLatch(1);

			httpAsyncClient.execute(httpGet, new FutureCallback<HttpResponse>() {

				@Override
				public void completed(final HttpResponse response) {

					try {
						byte[] content = EntityUtils.toByteArray(response.getEntity());
						System.out.println(JSON.parse(new String(content)));

					} catch (Exception e) {
						e.printStackTrace();
					}
					latch.countDown();

				}

				@Override
				public void failed(final Exception ex) {
					fail(ex.getMessage());

				}

				@Override
				public void cancelled() {
				}

			});

			latch.await();

		} finally {
			server.stop();
		}
	}

}
