package org.easycluster.easycluster.cluster.manager.event;

import org.easycluster.easycluster.core.Closure;

public interface EventHandler {

	/**
	 * 
	 * @param clusterEvent
	 */
	void handleClusterEvent(ClusterEvent clusterEvent);

	/**
	 * Subscribe to an event.
	 * 
	 * @param eventType
	 *            The event type
	 * @param closure
	 *            The callback function to be invoked when an event of type is
	 *            published.
	 */
	void registerObserver(String eventType, Closure closure);

}
