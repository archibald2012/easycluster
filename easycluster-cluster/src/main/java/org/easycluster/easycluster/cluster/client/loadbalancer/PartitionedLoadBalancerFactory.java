package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.Set;

import org.easycluster.easycluster.cluster.Node;


/**
 * A factory which can generate <code>PartitionedLoadBalancer</code>s.
 */
public interface PartitionedLoadBalancerFactory<PartitionedId> {

	/**
	 * Create a new load balancer instance based on the currently available <code>Node</code>s.
	 *
	 * @param nodes the currently available <code>Node</code>s in the cluster
	 *
	 * @return a new <code>PartitionedLoadBalancer</code> instance
	 */
	PartitionedLoadBalancer<PartitionedId> newLoadBalancer(Set<Node> nodes);
}
