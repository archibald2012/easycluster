package org.easycluster.easycluster.cluster.manager;

import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;


public interface ClusterListener {

	/**
	 * Handle the case that you are now connected to the cluster.
	 * 
	 * @param nodes
	 *          the current list of available <code>Node</code>s stored in the cluster metadata
	 */
	void handleClusterConnected(Set<Node> nodes);

	/**
	 * Handle the case that the cluster topology has changed.
	 * 
	 * @param nodes
	 *          the current list of available<code>Node</code>s stored in the cluster metadata
	 */
	void handleClusterNodesChanged(Set<Node> nodes);

	/**
	 * Handle the cluster event.
	 * 
	 * @param event
	 */
	void handleClusterEvent(ClusterEvent event);

	/**
	 * Handle the case that the cluster is now disconnected.
	 */
	void handleClusterDisconnected();

	/**
	 * Handle the case that the cluster is now shutdown.
	 */
	void handleClusterShutdown();
}
