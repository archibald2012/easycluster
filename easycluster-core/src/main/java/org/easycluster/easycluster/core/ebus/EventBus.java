package org.easycluster.easycluster.core.ebus;

/**
 * This is the event bus that manages the broadcasted events. Subscribes can
 * subscribe for event types with the bus without being aware of who will
 * eventually publish the event leading to very loosely coupled components.
 * 
 * @author wangqi
 * @version $Id: EventBus.java 14 2012-01-10 11:54:14Z archie $
 */
public interface EventBus {

	/**
	 * Subscribe to an event.
	 * 
	 * @param event
	 *            The event type
	 * @param clojure
	 *            The callback function to be invoked when a event of type
	 *            'etype' is published onto the bus.
	 * @return
	 */
	void subscribe(final String event, final Clojure clojure);

	/**
	 * Unsubscribe from the bus for the event type.
	 * 
	 * @param event
	 * @param clojure
	 */
	void unsubscribe(final String event, final Clojure clojure);

	/**
	 * Subscribe to an event.
	 * 
	 * @param event
	 *            The event type
	 * @param target
	 *            The target to be invoked when a event of type 'etype' is
	 *            published onto the bus.
	 * @param methodName
	 *            The method to be invoked when a event of type 'etype' is
	 *            published onto the bus.
	 */
	void subscribe(String event, Object target, String methodName);

	/**
	 * Unsubscribe from the bus for the event type.
	 * 
	 * @param event
	 * @param target
	 * @param methodName
	 */
	void unsubscribe(final String event, final Object target, final String methodName);

	/**
	 * Publish an event of a particular type and an associated object to carry
	 * extra information to the subscribers, to the bus.
	 * 
	 * @param event
	 *            The event type being published
	 * @param args
	 *            Object associated with the event that carries extra
	 *            information to subscribers.
	 */
	void publish(String event, Object... args);

}
