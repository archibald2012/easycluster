package org.easycluster.easycluster.cluster.exception;

public class InvalidMessageException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public InvalidMessageException(String message) {
		super(message);
	}

	public InvalidMessageException(String message, Throwable cause) {
		super(message, cause);
	}
}
