package org.easycluster.easycluster.cluster.exception;

public class InvalidClusterException extends ClusterException {

	private static final long	serialVersionUID	= 1L;

	public InvalidClusterException(String message) {
		super(message);
	}

	public InvalidClusterException(String message, Throwable cause) {
		super(message, cause);
	}
}
