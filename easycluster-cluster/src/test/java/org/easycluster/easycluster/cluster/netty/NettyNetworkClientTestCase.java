package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.NettyNetworkClient;
import org.easycluster.easycluster.cluster.netty.NettyNetworkServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NettyNetworkClientTestCase {

	private NettyNetworkClient	nettyNetworkClient;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = InvalidMessageException.class)
	public void testInvalidMessage() throws Exception {

		nettyNetworkClient = new NettyNetworkClient("app", "test", "127.0.0.1:2181", new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.start();

		nettyNetworkClient.sendMessage("teststring");

	}

	@Test
	public void testSend() throws Exception {

		NettyNetworkServer nettyNetworkServer = new NettyNetworkServer("app", "test", "127.0.0.1:2181");
		nettyNetworkServer.setPort(1000);
		nettyNetworkServer.start();

		nettyNetworkClient = new NettyNetworkClient("app", "test", "127.0.0.1:2181", new RoundRobinLoadBalancerFactory());
		nettyNetworkClient.registerRequest(String.class, null);
		nettyNetworkClient.start();

		nettyNetworkClient.sendMessage("teststring");

		nettyNetworkClient.stop();
	}
}
