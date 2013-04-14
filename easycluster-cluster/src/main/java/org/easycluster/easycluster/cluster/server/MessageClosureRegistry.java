package org.easycluster.easycluster.cluster.server;

import java.util.HashMap;
import java.util.Map;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.server.MessageClosure;

@SuppressWarnings("rawtypes")
public class MessageClosureRegistry {

	private Map<String, HandlerTuple> handlerMap = new HashMap<String, HandlerTuple>();

	public void registerHandler(Class<?> requestType, Class<?> responseType,
			MessageClosure handler) {
		if (requestType == null || handler == null) {
			throw new IllegalArgumentException(
					"requestType is null or handler is null.");
		}
		
		handlerMap.put(requestType.getName(), new HandlerTuple(requestType,
				responseType, handler));
	}

	public MessageClosure getHandlerFor(Object requestMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null");
		}

		return getHandlerTuple(requestMessage.getClass()).getHandler();
	}

	public Class<?> getResponseFor(Object requestMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null");
		}

		return getHandlerTuple(requestMessage.getClass()).getResponseType();
	}

	public boolean messageRegistered(Class<?> requestType) {
		return handlerMap.containsKey(requestType.getName());
	}

	public boolean validResponseFor(Object requestMessage,
			Object responseMessage) {
		if (requestMessage == null) {
			throw new IllegalArgumentException("requestMessage is null.");
		}

		Class<?> responseType = getHandlerTuple(requestMessage.getClass())
				.getResponseType();
		if (responseType == null && responseMessage == null) {
			return true;
		} else if (responseType != null && responseMessage == null) {
			return false;
		} else {
			return responseType == responseMessage.getClass();
		}
	}

	private HandlerTuple getHandlerTuple(Class<?> requestType) {
		String name = requestType.getName();
		if (handlerMap.containsKey(name)) {
			return handlerMap.get(name);
		} else {
			throw new InvalidMessageException(
					"No such message handler of type " + name + " registered");
		}
	}

	class HandlerTuple {
		private Class<?> requestType;
		private Class<?> responseType;
		private MessageClosure handler;

		public HandlerTuple(Class<?> requestType, Class<?> responseType,
				MessageClosure handler) {
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

	}
}
