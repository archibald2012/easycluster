package org.easycluster.easycluster.cluster.client.loadbalancer;

import org.easycluster.easycluster.cluster.Node;

/**
 * A <code>LoadBalancer</code> handles calculating the next <code>Node</code> a message should be routed to.
 */
public interface LoadBalancer {
	/**
	 * Returns the next <code>Node</code> a message should be routed to.
	 *
	 * @return the <code>Node</code> to route the next message to or null if there are no <code>Node</code>s available
	 */
	Node nextNode();
}
