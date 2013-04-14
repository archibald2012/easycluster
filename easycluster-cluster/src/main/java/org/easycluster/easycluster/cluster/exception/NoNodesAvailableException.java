package org.easycluster.easycluster.cluster.exception;

public class NoNodesAvailableException extends NetworkingException {

	private static final long	serialVersionUID	= 1L;

	public NoNodesAvailableException(String message) {
		super(message);
	}

	public NoNodesAvailableException(String message, Throwable cause) {
		super(message, cause);
	}
}
