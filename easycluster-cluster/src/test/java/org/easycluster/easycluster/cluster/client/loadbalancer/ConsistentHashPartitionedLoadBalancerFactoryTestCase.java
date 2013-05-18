package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.HashSet;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.ConsistentHashPartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.client.loadbalancer.PartitionedLoadBalancer;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class ConsistentHashPartitionedLoadBalancerFactoryTestCase {

	private ConsistentHashPartitionedLoadBalancerFactory<EId> loadBalancerFactory = new ConsistentHashPartitionedLoadBalancerFactory<EId>(
			5) {

		@Override
		protected int hashPartitionedId(EId id) {
			return id.hashCode();
		}
	};

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testNextNode() {
		Set<Node> nodes = new HashSet<Node>();
		nodes.add(new Node("localhost", 1111, new Integer[] { 0, 1 }));
		nodes.add(new Node("localhost", 1112, new Integer[] { 1, 2 }));
		nodes.add(new Node("localhost", 1113, new Integer[] { 2, 3 }));
		nodes.add(new Node("localhost", 1114, new Integer[] { 3, 4 }));
		nodes.add(new Node("localhost", 1115, new Integer[] { 4, 0 }));
		PartitionedLoadBalancer<EId> lb = loadBalancerFactory
				.newLoadBalancer(nodes);

		Set<Node> expected = new HashSet<Node>();
		expected.add(new Node("localhost", 1111, new Integer[] { 0, 1 }));
		expected.add(new Node("localhost", 1115, new Integer[] { 4, 0 }));

		for (int i = 0; i < 100; i++) {
			Node node = lb.nextNode(new EId(1210));
			System.out.println("nextNode: " + node);
			Assert.assertTrue(expected.contains(node));
		}
	}

}

class EId {
	private Integer id;

	public EId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
