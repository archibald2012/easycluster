package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;

public class RoundRobinPartitionedLoadBalancerFactory implements PartitionedLoadBalancerFactory<Integer> {

	public PartitionedLoadBalancer<Integer> newLoadBalancer(Set<Node> nodes) {
		for (Node node : nodes) {
			if (node.getPartitions() == null || node.getPartitions().length == 0) {
				throw new InvalidNodeException("No partitioned id(s) found. node=[" + node + "]");
			}
		}
		return new RoundRobinPartitionedLoadBalancer(nodes);
	}

	private class RoundRobinPartitionedLoadBalancer implements PartitionedLoadBalancer<Integer> {
		private final Map<Integer, RoundRobinLoadBalancer>	loadBalancerMap	= new HashMap<Integer, RoundRobinLoadBalancer>();

		private RoundRobinPartitionedLoadBalancer(Set<Node> nodes) {
			Map<Integer, Set<Node>> nodeMap = new HashMap<Integer, Set<Node>>();
			for (Node node : nodes) {
				for (Integer partitionId : node.getPartitions()) {
					Set<Node> nodeSet = nodeMap.get(partitionId);
					if (nodeSet == null) {
						nodeSet = new HashSet<Node>();
						nodeMap.put(partitionId, nodeSet);
					}
					nodeSet.add(node);
				}
			}
			for (Integer partitionedId : nodeMap.keySet()) {
				loadBalancerMap.put(partitionedId, new RoundRobinLoadBalancer(nodeMap.get(partitionedId)));
			}

		}

		public Node nextNode(Integer partitionedId) {
			RoundRobinLoadBalancer loadBalancer = loadBalancerMap.get(partitionedId);
			return loadBalancer == null ? null : loadBalancer.nextNode();
		}
	}
}
