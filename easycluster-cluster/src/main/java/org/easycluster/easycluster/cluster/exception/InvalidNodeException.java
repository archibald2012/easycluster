package org.easycluster.easycluster.cluster.exception;

public class InvalidNodeException extends ClusterException {

	private static final long	serialVersionUID	= 1L;

	public InvalidNodeException(String message) {
		super(message);
	}

	public InvalidNodeException(String message, Throwable cause) {
		super(message, cause);
	}
}
