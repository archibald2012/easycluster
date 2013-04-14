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
	void removeNode(int nodeId);

	/**
	 * 
	 * @param nodeId
	 */
	void markNodeAvailable(int nodeId);

	/**
	 * 
	 * @param nodeId
	 */
	void markNodeUnavailable(int nodeId);

	/**
	 * 
	 */
	void shutdown();
}
