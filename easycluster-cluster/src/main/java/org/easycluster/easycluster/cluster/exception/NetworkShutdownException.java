package org.easycluster.easycluster.cluster.exception;

public class NetworkShutdownException extends NetworkingException {

	private static final long	serialVersionUID	= 1L;

	public NetworkShutdownException() {
		super();
	}

	public NetworkShutdownException(String message) {
		super(message);
	}

	public NetworkShutdownException(String message, Throwable cause) {
		super(message, cause);
	}
}
