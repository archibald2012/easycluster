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

	private ConsistentHashPartitionedLoadBalancerFactory<EId>	loadBalancerFactory	= new ConsistentHashPartitionedLoadBalancerFactory<EId>(
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
		nodes.add(new Node(1, "localhost", 1111, new int[] { 0, 1 }, true));
		nodes.add(new Node(2, "localhost", 1112, new int[] { 1, 2 }, true));
		nodes.add(new Node(3, "localhost", 1113, new int[] { 2, 3 }, true));
		nodes.add(new Node(4, "localhost", 1114, new int[] { 3, 4 }, true));
		nodes.add(new Node(5, "localhost", 1115, new int[] { 4, 0 }, true));
		PartitionedLoadBalancer<EId> lb = loadBalancerFactory.newLoadBalancer(nodes);

		Set<Node> expected = new HashSet<Node>();
		expected.add(new Node(1, "localhost", 1111, new int[] { 0, 1 }, true));
		expected.add(new Node(5, "localhost", 1115, new int[] { 4, 0 }, true));

		for (int i = 0; i < 100; i++) {
			Node node = lb.nextNode(new EId(1210));
			System.out.println("nextNode: " + node);
			Assert.assertTrue(expected.contains(node));
		}
	}

}

class EId {
	private Integer	id;

	public EId(Integer id) {
		this.id = id;
	}

	@Override
	public int hashCode() {
		return id.hashCode();
	}
}
