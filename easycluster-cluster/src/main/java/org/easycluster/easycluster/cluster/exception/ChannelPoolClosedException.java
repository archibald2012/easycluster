package org.easycluster.easycluster.cluster.exception;

public class ChannelPoolClosedException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public ChannelPoolClosedException(String message) {
		super(message);
	}

	public ChannelPoolClosedException(String message, Throwable cause) {
		super(message, cause);
	}
}
