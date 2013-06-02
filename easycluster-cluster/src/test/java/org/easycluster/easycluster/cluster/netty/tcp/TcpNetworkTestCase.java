package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.SampleMessageClosure;
import org.easycluster.easycluster.cluster.SampleRequest;
import org.easycluster.easycluster.cluster.SampleResponse;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.GetBagItemsReq;
import org.easycluster.easycluster.cluster.netty.GetBagItemsResp;
import org.easycluster.easycluster.cluster.server.MessageClosure;
import org.easycluster.easycluster.serialization.protocol.meta.MetainfoUtils;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class TcpNetworkTestCase {

	private TcpNetworkClient	nettyNetworkClient;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = InvalidMessageException.class)
	public void testInvalidMessage() throws Exception {

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		nettyNetworkClient = new TcpNetworkClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.start();
		nettyNetworkClient.sendMessage("teststring");
	}

	@Test
	public void testSend() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);
		NettyBeanDecoder decoder = new NettyBeanDecoder();
		decoder.setTypeMetaInfo(typeMetaInfo);
		decoder.setDebugEnabled(true);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setDecoder(decoder);
		serverConfig.setEncoder(new NettyBeanEncoder());

		TcpNetworkServer nettyNetworkServer = new TcpNetworkServer(serverConfig);
		nettyNetworkServer.registerHandler(SampleRequest.class, SampleResponse.class, new SampleMessageClosure());
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		NettyBeanDecoder decoder2 = new NettyBeanDecoder();
		decoder2.setTypeMetaInfo(typeMetaInfo);
		decoder2.setDebugEnabled(false);
		clientConfig.setDecoder(decoder2);
		clientConfig.setEncoder(new NettyBeanEncoder());

		TcpNetworkClient nettyNetworkClient = new TcpNetworkClient(clientConfig, new RoundRobinLoadBalancerFactory());
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

	@Test
	public void testSend_binary() throws Exception {

		List<String> packages = new ArrayList<String>();
		packages.add("org.easycluster.easycluster.cluster.netty");
		MsgCode2TypeMetainfo typeMetaInfo = MetainfoUtils.createTypeMetainfo(packages);
		NettyBeanDecoder decoder = new NettyBeanDecoder();
		decoder.setTypeMetaInfo(typeMetaInfo);
		decoder.setDebugEnabled(true);

		NetworkServerConfig serverConfig = new NetworkServerConfig();
		serverConfig.setApplicationName("app");
		serverConfig.setServiceName("test");
		serverConfig.setZooKeeperConnectString("127.0.0.1:2181");
		serverConfig.setPort(6000);
		serverConfig.setDecoder(decoder);
		NettyBeanEncoder encoder = new NettyBeanEncoder();
		encoder.setDebugEnabled(false);
		serverConfig.setEncoder(encoder);

		TcpNetworkServer nettyNetworkServer = new TcpNetworkServer(serverConfig);
		nettyNetworkServer.start();

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setApplicationName("app");
		clientConfig.setServiceName("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");
		NettyBeanEncoder encoder2 = new NettyBeanEncoder();
		encoder2.setDebugEnabled(false);
		clientConfig.setEncoder(encoder2);
		NettyBeanDecoder decoder2 = new NettyBeanDecoder();
		decoder2.setTypeMetaInfo(typeMetaInfo);
		decoder2.setDebugEnabled(false);
		clientConfig.setDecoder(decoder2);

		TcpNetworkClient nettyNetworkClient = new TcpNetworkClient(clientConfig, new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(GetBagItemsReq.class, GetBagItemsResp.class);
		nettyNetworkClient.start();

		int num = 50000;

		List<GetBagItemsReq> client1Requests = new ArrayList<GetBagItemsReq>();

		for (int i = 0; i < num; i++) {
			GetBagItemsReq req = new GetBagItemsReq();
			req.setPlayerId(19063);
			req.setType(1);

			client1Requests.add(req);
		}

		final AtomicInteger count = new AtomicInteger();
		nettyNetworkServer.registerHandler(GetBagItemsReq.class, GetBagItemsResp.class, new MessageClosure<GetBagItemsReq, GetBagItemsResp>() {

			@Override
			public GetBagItemsResp execute(GetBagItemsReq input) {
				System.out.println("received : " + count.incrementAndGet());
				GetBagItemsResp response = new GetBagItemsResp();

				return response;
			}
		});

		long startTime = System.nanoTime();

		final List<Future<Object>> futures = new ArrayList<Future<Object>>(num);

		for (int i = 0; i < num; i++) {
			futures.add(nettyNetworkClient.sendMessage(client1Requests.get(i)));
		}

		final List<GetBagItemsResp> client1Responses = new ArrayList<GetBagItemsResp>();
		for (int i = 0; i < num; i++) {
			client1Responses.add((GetBagItemsResp) futures.get(i).get(1800, TimeUnit.SECONDS));
		}
		Assert.assertEquals(num, client1Responses.size());

		long endTime = System.nanoTime();
		System.out.println("Runtime estimated: " + (endTime - startTime) / 1000000 + "ms.");

		nettyNetworkClient.stop();
		nettyNetworkServer.stop();
	}
}
