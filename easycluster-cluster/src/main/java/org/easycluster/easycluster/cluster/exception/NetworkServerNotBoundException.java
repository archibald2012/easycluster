package org.easycluster.easycluster.cluster.exception;

public class NetworkServerNotBoundException extends NetworkingException {

	private static final long	serialVersionUID	= 1L;

	public NetworkServerNotBoundException() {
		super();
	}

	public NetworkServerNotBoundException(String message) {
		super(message);
	}

	public NetworkServerNotBoundException(String message, Throwable cause) {
		super(message, cause);
	}
}
