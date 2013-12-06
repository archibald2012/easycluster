package org.easycluster.easycluster.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.serialization.SerializeType;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.sun.net.httpserver.HttpServer;

public class WebSocketNetworkTestCase {

	private WebSocketServer	server;
	private WebSocketClient	client;

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
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.websocket");
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

		server = new WebSocketServer(serverConfig);
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

		client = new WebSocketClient(clientConfig, new RoundRobinLoadBalancerFactory());
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
	public void testOpenUrl() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.websocket");
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

		WebSocketServer server = new WebSocketServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		Thread.sleep(120000);
		server.stop();
	}
}
