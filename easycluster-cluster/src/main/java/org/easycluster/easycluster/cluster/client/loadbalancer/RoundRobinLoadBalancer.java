package org.easycluster.easycluster.cluster.client.loadbalancer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

import org.easycluster.easycluster.cluster.Node;

public class RoundRobinLoadBalancer implements LoadBalancer {
	private final List<Node>	nodes	= new ArrayList<Node>();
	private final AtomicInteger	index	= new AtomicInteger(0);

	RoundRobinLoadBalancer(Set<Node> nodes) {
		this.nodes.addAll(nodes);
		Collections.sort(this.nodes);
	}

	public Node nextNode() {
		if (nodes.isEmpty()) {
			return null;
		}
		int next = index.getAndIncrement();
		if (next < 0) {
			next = 0;
			index.set(next);
		}
		return nodes.get(next % nodes.size());
	}

}
