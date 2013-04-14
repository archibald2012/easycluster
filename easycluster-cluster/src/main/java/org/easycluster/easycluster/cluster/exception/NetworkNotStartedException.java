package org.easycluster.easycluster.cluster.exception;

/**
 * Exception that indicates that a method has been called before the networking system has been started.
 *
 */
public class NetworkNotStartedException extends NetworkingException {

	private static final long	serialVersionUID	= 1L;

	public NetworkNotStartedException() {
		super();
	}
	
	public NetworkNotStartedException(String message) {
		super(message);
	}

	public NetworkNotStartedException(String message, Throwable cause) {
		super(message, cause);
	}

}
