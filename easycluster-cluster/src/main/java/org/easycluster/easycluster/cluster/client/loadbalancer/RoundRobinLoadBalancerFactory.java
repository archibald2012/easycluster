package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.Set;

import org.easycluster.easycluster.cluster.Node;

public class RoundRobinLoadBalancerFactory implements LoadBalancerFactory {
	public LoadBalancer newLoadBalancer(Set<Node> nodes) {
		return new RoundRobinLoadBalancer(nodes);
	}

}
