package org.easycluster.easycluster.cluster.netty.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class WebsocketNetworkTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBind() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setPartitions(new Integer[] { 0, 1 });
		BinaryWebSocketFrameDecoder decoder = new BinaryWebSocketFrameDecoder();
		decoder.setTypeMetaInfo(typeMetaInfo);
		serverConfig.setDecoder(decoder);
		serverConfig.setEncoder(new BinaryWebSocketFrameEncoder());

		WebSocketNetworkServer server = new WebSocketNetworkServer(serverConfig);
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		clientConfig.setDecoder(decoder);
		clientConfig.setEncoder(new BinaryWebSocketFrameEncoder());

		WebSocketNetworkClient client = new WebSocketNetworkClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

		System.out.println("Result: " + future.get(20000, TimeUnit.SECONDS));
	}

}
