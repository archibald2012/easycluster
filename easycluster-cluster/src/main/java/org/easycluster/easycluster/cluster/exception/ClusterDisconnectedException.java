package org.easycluster.easycluster.cluster.exception;

public class ClusterDisconnectedException extends ClusterException {

	private static final long	serialVersionUID	= 1L;

	public ClusterDisconnectedException() {
		super();
	}

	public ClusterDisconnectedException(String message) {
		super(message);
	}

	public ClusterDisconnectedException(String message, Throwable cause) {
		super(message, cause);
	}
}
