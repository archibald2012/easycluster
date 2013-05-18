/**
 * 
 */
package org.easycluster.easycluster.core;

public class TransportUtil {
	private static final String TRANSPORT_SENDER = "TRANSPORT_SENDER";

	public static Object attachSender(Object propertyable, Sender sender) {
		if (propertyable instanceof Propertyable) {
			((Propertyable) propertyable).setProperty(TRANSPORT_SENDER, sender);
		}

		return propertyable;
	}

	public static Sender getSenderOf(Object propertyable) {
		if (propertyable instanceof Propertyable) {
			return (Sender) ((Propertyable) propertyable)
					.getProperty(TRANSPORT_SENDER);
		}
		return null;
	}

}
