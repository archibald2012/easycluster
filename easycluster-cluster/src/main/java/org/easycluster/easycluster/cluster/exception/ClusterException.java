package org.easycluster.easycluster.cluster.exception;

public class ClusterException extends RuntimeException {

	private static final long	serialVersionUID	= 1L;

	public ClusterException() {
		super();
	}

	public ClusterException(String message) {
		super(message);
	}

	public ClusterException(String message, Throwable cause) {
		super(message, cause);
	}
}
