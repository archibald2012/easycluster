package org.easycluster.easycluster.cluster.exception;

public class ClusterNotStartedException extends ClusterException {

	private static final long	serialVersionUID	= 1L;

	public ClusterNotStartedException() {
		super();
	}

	public ClusterNotStartedException(String message) {
		super(message);
	}

	public ClusterNotStartedException(String message, Throwable cause) {
		super(message, cause);
	}
}
