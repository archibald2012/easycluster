package org.easycluster.easycluster.cluster.client;

import static org.easymock.EasyMock.anyObject;
import static org.easymock.EasyMock.createControl;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.expectLastCall;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.ClusterIoClient;
import org.easycluster.easycluster.cluster.client.NetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancer;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.exception.NoNodesAvailableException;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.ClusterListener;
import org.easycluster.easycluster.core.Closure;
import org.easymock.IMocksControl;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class NetworkClientTestCase {

	private NetworkClient		networkClient;
	private IMocksControl		mockControl;
	private LoadBalancerFactory	loadBalancerFactory;
	private MessageRegistry		messageRegistry;
	private ClusterIoClient		clusterIoClient;
	private ClusterClient		clusterClient;

	@Before
	public void setUp() throws Exception {
		mockControl = createControl();

		clusterIoClient = mockControl.createMock(ClusterIoClient.class);
		clusterClient = mockControl.createMock(ClusterClient.class);
		loadBalancerFactory = mockControl.createMock(LoadBalancerFactory.class);
		messageRegistry = mockControl.createMock(MessageRegistry.class);

		NetworkClientConfig clientConfig = new NetworkClientConfig();
		clientConfig.setServiceGroup("app");
		clientConfig.setService("test");
		clientConfig.setZooKeeperConnectString("127.0.0.1:2181");

		networkClient = new NetworkClient(clientConfig, loadBalancerFactory);

	}

	@After
	public void tearDown() throws Exception {
		mockControl = null;
	}

	@Test(expected = NoNodesAvailableException.class)
	public void testSendMessage_NoNode() throws Exception {

		Set<Node> nodeSet = new HashSet<Node>();

		expect(messageRegistry.contains(anyObject())).andReturn(true);
		networkClient.setMessageRegistry(messageRegistry);

		expect(clusterClient.getNodes()).andReturn(nodeSet);

		LoadBalancer loadBalancer = mockControl.createMock(LoadBalancer.class);
		expect(loadBalancerFactory.newLoadBalancer(nodeSet)).andReturn(loadBalancer);
		expect(loadBalancer.nextNode()).andReturn(null);

		expect(clusterClient.isConnected()).andReturn(true);
		networkClient.setClusterClient(clusterClient);

		clusterIoClient.nodesChanged(nodeSet);
		expectLastCall().times(1);
		networkClient.setClusterIoClient(clusterIoClient);

		clusterClient.start();
		expectLastCall().times(1);
		clusterClient.awaitConnectionUninterruptibly();
		expectLastCall().times(1);
		expect(clusterClient.addListener((ClusterListener) anyObject())).andReturn(1L);

		mockControl.replay();

		networkClient.updateLoadBalancer(nodeSet);
		networkClient.sendMessage("teststring");

		mockControl.verify();
	}

	@Test
	public void testSendMessage() throws Exception {

		Set<Node> nodeSet = new HashSet<Node>();
		Node node = new Node("127.0.0.1", 1111, new Integer[0]);
		nodeSet.add(node);

		expect(messageRegistry.contains(anyObject())).andReturn(true);
		networkClient.setMessageRegistry(messageRegistry);

		expect(clusterClient.getNodes()).andReturn(nodeSet);

		LoadBalancer loadBalancer = mockControl.createMock(LoadBalancer.class);
		expect(loadBalancerFactory.newLoadBalancer(nodeSet)).andReturn(loadBalancer).times(2);

		expect(loadBalancer.nextNode()).andReturn(node);

		expect(clusterClient.isConnected()).andReturn(true);
		networkClient.setClusterClient(clusterClient);

		clusterIoClient.nodesChanged(nodeSet);
		expectLastCall().times(1);
		clusterIoClient.sendMessage(anyObject(Node.class), anyObject(String.class), anyObject(Closure.class));
		expectLastCall().times(1);
		networkClient.setClusterIoClient(clusterIoClient);

		clusterClient.start();
		expectLastCall().times(1);
		clusterClient.awaitConnectionUninterruptibly();
		expectLastCall().times(1);
		expect(clusterClient.addListener((ClusterListener) anyObject())).andReturn(1L);

		mockControl.replay();

		networkClient.updateLoadBalancer(nodeSet);
		Future<Object> f = networkClient.sendMessage("teststring");
		Assert.assertNotNull(f);
		mockControl.verify();
	}
}
