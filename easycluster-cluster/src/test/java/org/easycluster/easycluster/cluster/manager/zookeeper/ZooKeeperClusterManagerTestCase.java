package org.easycluster.easycluster.cluster.manager.zookeeper;

import junit.framework.Assert;

import org.easycluster.easycluster.cluster.Node;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class ZooKeeperClusterManagerTestCase {

	private ZooKeeperClusterManager	clusterManager;

	private ZooKeeperClusterManager	clusterManager2;

	@Before
	public void setUp() throws Exception {

	}

	@After
	public void tearDown() throws Exception {

	}

	@Test
	public void testNonMutexNode_removeNode() throws Exception {

		clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager.start();

		clusterManager2 = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager2.start();
		Thread.sleep(2000);

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.addNode(node);
			clusterManager.markNodeAvailable(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(i + 1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(i + 1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(node));
			Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(node));
		}

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.removeNode(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(10 - i - 1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(10 - i - 1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertFalse(clusterManager.getClusterNotification().getAvailableNodes().contains(node));
			Assert.assertFalse(clusterManager2.getClusterNotification().getAvailableNodes().contains(node));
		}

		clusterManager.shutdown();
		clusterManager2.shutdown();
	}

	@Test
	public void testNonMutexNode() throws Exception {

		clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager.start();

		clusterManager2 = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, false);
		clusterManager2.start();
		Thread.sleep(2000);

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.addNode(node);
			clusterManager.markNodeAvailable(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(i + 1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(i + 1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(node));
			Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(node));
		}

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.markNodeUnavailable(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(10 - i - 1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(10 - i - 1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertFalse(clusterManager.getClusterNotification().getAvailableNodes().contains(node));
			Assert.assertFalse(clusterManager2.getClusterNotification().getAvailableNodes().contains(node));
		}

		clusterManager.shutdown();
		clusterManager2.shutdown();
	}

	@Test
	public void testMutexNode() throws Exception {

		clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, true);
		clusterManager.start();

		clusterManager2 = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, true);
		clusterManager2.start();
		Thread.sleep(2000);

		Node assertNode = new Node("localhost", 1111);
		assertNode.setServiceGroup("app");
		assertNode.setService("ZooKeeperClusterManagerTestCase");

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.addNode(node);
			clusterManager.markNodeAvailable(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(assertNode));
			Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(assertNode));
		}

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.markNodeUnavailable(node.getId());
			Thread.sleep(1000);

			if (i < 9) {
				Assert.assertEquals(1, clusterManager.getClusterNotification().getAvailableNodes().size());
				Assert.assertEquals(1, clusterManager2.getClusterNotification().getAvailableNodes().size());
				Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(new Node("localhost", 1111 + i + 1)));
				Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(new Node("localhost", 1111 + i + 1)));
			} else {
				Assert.assertEquals(0, clusterManager.getClusterNotification().getAvailableNodes().size());
				Assert.assertEquals(0, clusterManager2.getClusterNotification().getAvailableNodes().size());

			}
		}

		clusterManager.shutdown();
		clusterManager2.shutdown();
	}

	@Test
	public void testMutexNode_removeNode() throws Exception {

		clusterManager = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, true);
		clusterManager.start();

		clusterManager2 = new ZooKeeperClusterManager("app", "ZooKeeperClusterManagerTestCase", "127.0.0.1:2181", 30000, true);
		clusterManager2.start();
		Thread.sleep(2000);

		Node assertNode = new Node("localhost", 1111);
		assertNode.setServiceGroup("app");
		assertNode.setService("ZooKeeperClusterManagerTestCase");

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.addNode(node);
			clusterManager.markNodeAvailable(node.getId());
			Thread.sleep(1000);

			Assert.assertEquals(1, clusterManager.getClusterNotification().getAvailableNodes().size());
			Assert.assertEquals(1, clusterManager2.getClusterNotification().getAvailableNodes().size());
			Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(assertNode));
			Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(assertNode));
		}

		for (int i = 0; i < 10; i++) {
			Node node = new Node("localhost", 1111 + i);
			node.setServiceGroup("app");
			node.setService("ZooKeeperClusterManagerTestCase");
			clusterManager.removeNode(node.getId());
			Thread.sleep(1000);

			if (i < 9) {
				Assert.assertEquals(1, clusterManager.getClusterNotification().getAvailableNodes().size());
				Assert.assertEquals(1, clusterManager2.getClusterNotification().getAvailableNodes().size());
				Assert.assertTrue(clusterManager.getClusterNotification().getAvailableNodes().contains(new Node("localhost", 1111 + i + 1)));
				Assert.assertTrue(clusterManager2.getClusterNotification().getAvailableNodes().contains(new Node("localhost", 1111 + i + 1)));
			} else {
				Assert.assertEquals(0, clusterManager.getClusterNotification().getAvailableNodes().size());
				Assert.assertEquals(0, clusterManager2.getClusterNotification().getAvailableNodes().size());

			}
		}

		clusterManager.shutdown();
		clusterManager2.shutdown();
	}
}
