package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.Set;

import org.easycluster.easycluster.cluster.Node;


/**
 * A factory which can generate <code>LoadBalancer</code>s.
 */
public interface LoadBalancerFactory {

	/**
	 * Create a new load balancer instance based on the currently available <code>Node</code>s.
	 *
	 * @param nodes the currently available <code>Node</code>s in the cluster
	 *
	 * @return a new <code>LoadBalancer</code> instance
	 */
	LoadBalancer newLoadBalancer(Set<Node> nodes);
}
