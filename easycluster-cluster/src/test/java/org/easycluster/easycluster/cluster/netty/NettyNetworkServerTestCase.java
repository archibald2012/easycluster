package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.cluster.exception.NetworkingException;
import org.easycluster.easycluster.cluster.netty.tcp.TcpNetworkServer;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


public class NettyNetworkServerTestCase {

	private TcpNetworkServer	nettyNetworkServer;

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testBind() throws Exception {
		nettyNetworkServer = new TcpNetworkServer("app", "test", "127.0.0.1:2181");
		nettyNetworkServer.setPort(1000);
		nettyNetworkServer.start();

		Thread.sleep(2000);

		nettyNetworkServer.stop();
	}

	@Test(expected = NetworkingException.class)
	public void testBindRetry() throws Exception {
		nettyNetworkServer = new TcpNetworkServer("app", "test", "127.0.0.1:2181");
		nettyNetworkServer.setPort(1000);
		nettyNetworkServer.start();

		Thread.sleep(2000);

		nettyNetworkServer.setPort(1000);
		nettyNetworkServer.start();
	}
}
