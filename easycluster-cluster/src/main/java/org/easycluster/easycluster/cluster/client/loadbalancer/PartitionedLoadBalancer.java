package org.easycluster.easycluster.cluster.client.loadbalancer;

import org.easycluster.easycluster.cluster.Node;

/**
 * A <code>PartitionedLoadBalancer</code> handles calculating the next <code>Node</code> a message should be routed to
 * based on a PartitionedId.
 */
public interface PartitionedLoadBalancer<PartitionedId> {

	/**
	 * Returns the next <code>Node</code> a message should be routed to based on the PartitionId provided.
	 *
	 * @param id the id to be used to calculate partitioning information.
	 *
	 * @return the <code>Node</code> to route the next message to
	 */
	Node nextNode(PartitionedId id);
}
