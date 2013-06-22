package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.Arrays;
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
		nodes.add(new Node("localhost", 1111, Arrays.asList(new Integer[] { 0, 1 })));
		nodes.add(new Node("localhost", 1112, Arrays.asList(new Integer[] { 1, 2 })));
		nodes.add(new Node("localhost", 1113, Arrays.asList(new Integer[] { 2, 3 })));
		nodes.add(new Node("localhost", 1114, Arrays.asList(new Integer[] { 3, 4 })));
		nodes.add(new Node("localhost", 1115, Arrays.asList(new Integer[] { 4, 0 })));
		PartitionedLoadBalancer<Integer> lb = loadBalancerFactory
				.newLoadBalancer(nodes);

		Set<Node> expected = new HashSet<Node>();
		expected.add(new Node("localhost", 1111, Arrays.asList(new Integer[] { 0, 1 })));
		expected.add(new Node("localhost", 1115, Arrays.asList(new Integer[] { 4, 0 })));

		for (int i = 0; i < 100; i++) {
			Node node = lb.nextNode(1210);
			System.out.println("nextNode: " + node);
			Assert.assertTrue(expected.contains(node));
		}
	}

}
