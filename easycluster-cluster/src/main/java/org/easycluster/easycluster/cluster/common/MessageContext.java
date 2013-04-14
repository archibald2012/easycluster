package org.easycluster.easycluster.cluster.common;

import org.easycluster.easycluster.core.Closure;

public class MessageContext {

	private Object	message;
	private Closure	closure;
	private long		timestamp;

	public MessageContext(Object message) {
		this.message = message;
		this.timestamp = System.currentTimeMillis();
	}

	public MessageContext(Object message, Closure closure) {
		this.message = message;
		this.closure = closure;
		this.timestamp = System.currentTimeMillis();
	}

	public Object getMessage() {
		return message;
	}

	public Closure getClosure() {
		return closure;
	}

	public long getTimestamp() {
		return timestamp;
	}

}
