package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.HashSet;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancer;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.client.loadbalancer.RoundRobinLoadBalancerFactory;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;


public class RoundRobinLoadBalancerFactoryTestCase {

	private LoadBalancerFactory	loadBalancerFactory	= new RoundRobinLoadBalancerFactory();

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextNode() {
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(new Node(1, "localhost", 1111, true));
		nodes.add(new Node(2, "localhost", 1112, true));
		nodes.add(new Node(3, "localhost", 1113, true));
		nodes.add(new Node(4, "localhost", 1114, true));
		nodes.add(new Node(5, "localhost", 1115, true));
		nodes.add(new Node(6, "localhost", 1116, true));
		nodes.add(new Node(7, "localhost", 1117, true));
		nodes.add(new Node(8, "localhost", 1118, true));
		nodes.add(new Node(9, "localhost", 1119, true));
		nodes.add(new Node(10, "localhost", 1120, true));
		nodes.add(new Node(11, "localhost", 1120, true));
		nodes.add(new Node(11, "localhost", 1120, true));

		for (Node node : nodes) {
			System.out.println("node: " + node);
			System.out.println("hashCode: " + node.hashCode());
		}

		LoadBalancer lb = loadBalancerFactory.newLoadBalancer(nodes);

		for (int i = 0; i < 100; i++) {
			Node node = lb.nextNode();
			System.out.println("nextNode: " + node);
			Assert.assertTrue(nodes.contains(node));
		}
	}

	@Test
	public void testEmptyNodes() {
		LoadBalancer lb = loadBalancerFactory.newLoadBalancer(new HashSet<Node>());
		Assert.assertNull(lb.nextNode());
	}

	@Test(expected = NullPointerException.class)
	public void testNull() {
		loadBalancerFactory.newLoadBalancer(null);
	}

}
