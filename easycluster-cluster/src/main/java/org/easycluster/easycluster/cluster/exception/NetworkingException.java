package org.easycluster.easycluster.cluster.exception;

public class NetworkingException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public NetworkingException() {
		super();
	}
	
	public NetworkingException(String message) {
		super(message);
	}

	public NetworkingException(String message, Throwable cause) {
		super(message, cause);
	}
}
