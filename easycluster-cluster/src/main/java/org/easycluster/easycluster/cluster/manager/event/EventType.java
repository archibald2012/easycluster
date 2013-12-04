package org.easycluster.easycluster.cluster.manager.event;

/**
 * The event type could be sent to a component for notification.
 * 
 */
public enum EventType {

	/**
	 * For modifying the log level.
	 */
	LOG_UPDATE,

	/**
	 * For modifying the metrics settings.
	 */
	METRICS_UPDATE,

	/**
	 * For updating the message registry.
	 */
	MESSAGE_FILTER,
	
	/**
	 * For updating the black list.
	 */
	BLACKLIST_UPDATE;
}
