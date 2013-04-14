package org.easycluster.easycluster.cluster.client;

import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.core.Closure;


public interface ClusterIoClient {

	void sendMessage(Node node, Object message, Closure responseCallback);

	void nodesChanged(Set<Node> nodes);

	void shutdown();
}
