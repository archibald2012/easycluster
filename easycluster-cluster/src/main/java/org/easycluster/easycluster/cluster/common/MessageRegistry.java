package org.easycluster.easycluster.cluster.common;

import java.util.HashMap;
import java.util.Map;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;


public class MessageRegistry {
	private Map<String, String>	messageMap	= new HashMap<String, String>();

	public boolean contains(Object request) {
		return messageMap.containsKey(request.getClass().getName());
	}

	public boolean hasResponse(Class<?> request) {
		return getMessagePair(request) != null;
	}

	public String getResponse(Class<?> request) {
		return getMessagePair(request);
	}

	public boolean registerMessage(Class<?> request, Class<?> responseMessage) {
		if (request == null)
			throw new IllegalArgumentException("request is null");

		String response = (responseMessage == null) ? null : responseMessage.getName();
		messageMap.put(request.getName(), response);
		return true;
	}

	public boolean validResponseFor(Class<?> request, Object responseMessage) {
		if (request == null || responseMessage == null) {
			throw new IllegalArgumentException("request or responseMessage is null");
		}

		String response = getMessagePair(request);
		return response.equals(responseMessage.getClass().getName());
	}

	private String getMessagePair(Class<?> request) {
		String name = request.getName();
		if (messageMap.containsKey(name)) {
			return messageMap.get(name);
		} else {
			throw new InvalidMessageException("No such message of type " + name + " registered");
		}
	}
}
