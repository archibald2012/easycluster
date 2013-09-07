package org.easycluster.easycluster.cluster.server;

import java.util.HashMap;
import java.util.Map;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("rawtypes")
public class MessageClosureRegistry {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(MessageClosureRegistry.class);

	private Map<String, HandlerTuple>	handlerMap	= new HashMap<String, HandlerTuple>();

	public void registerHandler(Class<?> requestType, Class<?> responseType, MessageClosure handler) {
		if (requestType == null || handler == null) {
			throw new IllegalArgumentException("requestType is null or handler is null.");
		}

		handlerMap.put(getComponentName(requestType), new HandlerTuple(requestType, responseType, handler));
	}

	public boolean updateFilter(String requestType, boolean canceled) {
		boolean oldValue = false;

		HandlerTuple result = handlerMap.get(requestType);
		if (result != null) {
			oldValue = result.isCanceled();
			result.setCanceled(canceled);
			handlerMap.put(requestType, result);
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Invalid requestType " + requestType + " canceled " + canceled);
			}
		}

		return oldValue;
	}

	public boolean messageRegistered(Class<?> requestType) {
		String name = getComponentName(requestType);
		HandlerTuple result = handlerMap.get(name);
		return (result != null && !result.isCanceled());
	}

	public MessageClosure getHandlerFor(Object requestMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null");
		}

		return getHandlerTuple(requestMessage.getClass()).getHandler();
	}

	public Class<?> getResponseTypeFor(Object requestMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null");
		}

		return getHandlerTuple(requestMessage.getClass()).getResponseType();
	}

	public boolean validResponseFor(Object requestMessage, Object responseMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null.");
		}

		Class<?> responseType = getHandlerTuple(requestMessage.getClass()).getResponseType();
		if (responseType == null && responseMessage == null) {
			return true;
		} else if (responseType != null && responseMessage == null) {
			return false;
		} else {
			return responseType == responseMessage.getClass();
		}
	}

	private String getComponentName(Class<?> requestType) {
		return requestType.getName();
	}

	private HandlerTuple getHandlerTuple(Class<?> requestType) {
		String name = getComponentName(requestType);
		if (handlerMap.containsKey(name)) {
			return handlerMap.get(name);
		} else {
			throw new InvalidMessageException("No such message handler of type " + name + " registered");
		}
	}

	class HandlerTuple {
		private Class<?>		requestType;
		private Class<?>		responseType;
		private MessageClosure	handler;
		private boolean			canceled;

		public HandlerTuple(Class<?> requestType, Class<?> responseType, MessageClosure handler) {
			this.requestType = requestType;
			this.responseType = responseType;
			this.handler = handler;
		}

		public Class<?> getRequestType() {
			return requestType;
		}

		public Class<?> getResponseType() {
			return responseType;
		}

		public MessageClosure getHandler() {
			return handler;
		}

		public boolean isCanceled() {
			return canceled;
		}

		public void setCanceled(boolean canceled) {
			this.canceled = canceled;
		}

	}
}
