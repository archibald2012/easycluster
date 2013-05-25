package org.easycluster.easycluster.cluster.netty.websocket;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

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

		WebSocketNetworkServer server = new WebSocketNetworkServer("app", "test", "127.0.0.1:2181");
		server.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		server.setPort(5000);
		server.setPartitionIds(new Integer[] { 0, 1 });
		BinaryWebSocketFrameDecoder decoder = new BinaryWebSocketFrameDecoder();
		decoder.setTypeMetaInfo(typeMetaInfo);
		server.setDecoder(decoder);
		server.setEncoder(new BinaryWebSocketFrameEncoder());
		server.start();

		WebSocketNetworkClient client = new WebSocketNetworkClient("app", "test", "127.0.0.1:2181", new RoundRobinLoadBalancerFactory());
		client.setDecoder(decoder);
		client.setEncoder(new BinaryWebSocketFrameEncoder());
		client.registerRequest(SampleRequest.class, SampleResponse.class);
		client.start();

		//Thread.sleep(20000);
		
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
