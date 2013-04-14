package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;


public abstract class ConsistentHashPartitionedLoadBalancerFactory<PartitionedId> implements
		PartitionedLoadBalancerFactory<PartitionedId> {

	private int	numPartitions;

	public ConsistentHashPartitionedLoadBalancerFactory(int numPartitions) {
		this.numPartitions = numPartitions;
	}

	public PartitionedLoadBalancer<PartitionedId> newLoadBalancer(Set<Node> nodes) {
		return new ConsistentHashLoadBalancer(nodes);
	}

	public int partitionForId(PartitionedId id) {
		return Math.abs(hashPartitionedId(id)) % numPartitions;
	}

	abstract protected int hashPartitionedId(PartitionedId id);

	private class ConsistentHashLoadBalancer implements PartitionedLoadBalancer<PartitionedId> {
		private final Map<Integer, List<Node>>	nodeMap	= new HashMap<Integer, List<Node>>();
		private final Random										random	= new Random();

		private ConsistentHashLoadBalancer(Set<Node> nodes) {
			for (Node node : nodes) {
				for (int partitionId : node.getPartitions()) {
					List<Node> nodeList = nodeMap.get(partitionId);
					if (nodeList == null) {
						nodeList = new ArrayList<Node>();
						nodeMap.put(partitionId, nodeList);
					}
					nodeList.add(node);
				}
			}
		}

		public Node nextNode(PartitionedId partitionedId) {
			List<Node> nodes = nodeMap.get(partitionForId(partitionedId));
			return nodes == null ? null : nodes.get(random.nextInt(nodes.size()));
		}
	}
}
