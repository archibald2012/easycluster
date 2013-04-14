package org.easycluster.easycluster.cluster;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.netty.NettyNetworkClient;
import org.easycluster.easycluster.cluster.netty.NettyNetworkServer;
import org.easycluster.easycluster.cluster.netty.codec.NettyBeanDecoder;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NettyNetworkClientTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBind() throws Exception {
		List<String> packages = new ArrayList<String>();
		packages.add("edu.hziee.common.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);
		NettyBeanDecoder decoder = new NettyBeanDecoder();
		decoder.setTypeMetaInfo(typeMetaInfo);

		NettyNetworkServer nettyNetworkServer = new NettyNetworkServer("app", "test", "127.0.0.1:2181");
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.setPort(1000);
		nettyNetworkServer.setPartitionIds(new int[] { 0, 1 });
		nettyNetworkServer.setDecoder(decoder);
		nettyNetworkServer.start();

		NettyNetworkClient nettyNetworkClient = new NettyNetworkClient("app", "test", "127.0.0.1:2181",
				new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.setDecoder(decoder);
		nettyNetworkClient.registerRequest(SampleRequest.class, SampleResponse.class);
		nettyNetworkClient.start();

		SampleRequest request = new SampleRequest();
		request.setIntField(1);
		request.setShortField((byte) 1);
		request.setByteField((byte) 1);
		request.setLongField(1L);
		request.setStringField("test");

		request.setByteArrayField(new byte[] { 127 });

		Future<Object> future = nettyNetworkClient.sendMessage(request);

		System.out.println("Result: " + future.get(20, TimeUnit.SECONDS));
	}

}
