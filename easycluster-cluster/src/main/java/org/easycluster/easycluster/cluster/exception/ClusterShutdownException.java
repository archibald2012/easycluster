package org.easycluster.easycluster.cluster.exception;

public class ClusterShutdownException extends ClusterException {

	private static final long	serialVersionUID	= 1L;

	public ClusterShutdownException() {
		super();
	}

	public ClusterShutdownException(String message) {
		super(message);
	}

	public ClusterShutdownException(String message, Throwable cause) {
		super(message, cause);
	}
}
