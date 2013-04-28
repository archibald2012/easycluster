package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.HashSet;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.IntegerConsistentHashPartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.client.loadbalancer.PartitionedLoadBalancer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class IntegerConsistentHashPartitionedLoadBalancerFactoryTestCase {

	private IntegerConsistentHashPartitionedLoadBalancerFactory loadBalancerFactory = new IntegerConsistentHashPartitionedLoadBalancerFactory(
			5);

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextNode() {
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(new Node("localhost", 1111, new int[] { 0, 1 }, true));
		nodes.add(new Node("localhost", 1112, new int[] { 1, 2 }, true));
		nodes.add(new Node("localhost", 1113, new int[] { 2, 3 }, true));
		nodes.add(new Node("localhost", 1114, new int[] { 3, 4 }, true));
		nodes.add(new Node("localhost", 1115, new int[] { 4, 0 }, true));
		PartitionedLoadBalancer<Integer> lb = loadBalancerFactory
				.newLoadBalancer(nodes);

		Set<Node> expected = new HashSet<Node>();
		expected.add(new Node("localhost", 1111, new int[] { 0, 1 }, true));
		expected.add(new Node("localhost", 1115, new int[] { 4, 0 }, true));

		for (int i = 0; i < 100; i++) {
			Node node = lb.nextNode(1210);
			System.out.println("nextNode: " + node);
			Assert.assertTrue(expected.contains(node));
		}
	}

}
