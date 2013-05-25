/**
 * 
 */
package org.easycluster.easycluster.core;

public class TransportUtil {
	private static final String	TRANSPORT_SENDER	= "TRANSPORT_SENDER";

	public static Object attachSender(Object propertyable, Sender sender) {
		if (propertyable instanceof Propertyable) {
			((Propertyable) propertyable).setProperty(TRANSPORT_SENDER, sender);
		}

		return propertyable;
	}

	public static Sender getSenderOf(Object propertyable) {
		if (propertyable instanceof Propertyable) {
			return (Sender) ((Propertyable) propertyable).getProperty(TRANSPORT_SENDER);
		}
		return null;
	}

	private static final String	REQUEST_OBJECT	= "REQUEST_OBJ";

	public static Object attachRequest(Object propertyable, Object request) {
		if (propertyable instanceof Propertyable) {
			((Propertyable) propertyable).setProperty(REQUEST_OBJECT, request);
		}

		return propertyable;
	}

	public static Object getRequestOf(Object propertyable) {
		if (propertyable instanceof Propertyable) {
			return ((Propertyable) propertyable).getProperty(REQUEST_OBJECT);
		}
		return null;
	}

}
