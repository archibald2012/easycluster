package org.easycluster.easycluster.cluster.manager;

import org.easycluster.easycluster.cluster.Node;

public interface ClusterManager {

	/**
	 * 
	 */
	void start();

	/**
	 * 
	 * @param node
	 */
	void addNode(Node node);

	/**
	 * 
	 * @param nodeId
	 */
	void removeNode(String nodeId);

	/**
	 * 
	 * @param nodeId
	 */
	void markNodeAvailable(String nodeId);

	/**
	 * 
	 * @param nodeId
	 */
	void markNodeUnavailable(String nodeId);

	/**
	 * 
	 */
	void shutdown();
}
