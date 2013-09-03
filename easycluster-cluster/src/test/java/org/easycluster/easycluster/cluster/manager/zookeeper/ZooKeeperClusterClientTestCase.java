package org.easycluster.easycluster.cluster.manager.zookeeper;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.exception.ClusterDisconnectedException;
import org.easycluster.easycluster.cluster.exception.ClusterNotStartedException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.manager.DefaultClusterClient;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZooKeeperClusterClientTestCase {

	private DefaultClusterClient	clusterClient;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {
	}

	@Test(expected = ClusterNotStartedException.class)
	public void testAddNode_ClusterNotStarted() {
		clusterClient = new DefaultClusterClient("app", "test");
		ZooKeeperClusterManager clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager.setClusterNotification(clusterClient.getClusterNotification());
		clusterClient.setClusterManager(clusterManager);
		assertFalse(clusterClient.isConnected());
		assertTrue(clusterClient.isShutdown());

		clusterClient.addNode(new Node("localhost", 1111));
	}

	@Test(expected = ClusterDisconnectedException.class)
	public void testAddNode_ClusterDisconnected() throws Exception {
		clusterClient = new DefaultClusterClient("app", "test");

		ZooKeeperClusterManager clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager.setClusterNotification(clusterClient.getClusterNotification());
		clusterClient.setClusterManager(clusterManager);
		clusterClient.start();

		Thread.sleep(100);

		assertTrue(clusterClient.isConnected());
		assertFalse(clusterClient.isShutdown());

		clusterManager.handleDisconnected();

		assertFalse(clusterClient.isConnected());
		assertFalse(clusterClient.isShutdown());
		
		clusterManager.handleConnected();

		assertTrue(clusterClient.isConnected());
		assertFalse(clusterClient.isShutdown());
		
		clusterManager.handleDisconnected();

		clusterClient.addNode(new Node("localhost", 1111));

	}

	@Test(expected = ClusterShutdownException.class)
	public void testAddNode_ClusterShutdown() throws Exception {
		clusterClient = new DefaultClusterClient("app", "test");
		ZooKeeperClusterManager clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager.setClusterNotification(clusterClient.getClusterNotification());
		clusterClient.setClusterManager(clusterManager);
		clusterClient.start();
		
		Thread.sleep(100);
		
		assertTrue(clusterClient.isConnected());

		clusterClient.shutdown();
		assertTrue(clusterClient.isShutdown());

		clusterClient.addNode(new Node("localhost", 1111));
	}

}
