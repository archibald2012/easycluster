package org.easycluster.easycluster.cluster.exception;

public class SerializeException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public SerializeException(String message) {
		super(message);
	}

	public SerializeException(String message, Throwable cause) {
		super(message, cause);
	}
}
