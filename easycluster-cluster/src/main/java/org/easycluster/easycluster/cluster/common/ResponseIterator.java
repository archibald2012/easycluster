package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * An iterator over the responses from a network request.
 */
public interface ResponseIterator {

	/**
	 * Calculates whether you have iterated over all of the responses. A return value of true indicates
	 * that there are more responses, it does not indicate that those responses have been received and
	 * are immediately available for processing.
	 *
	 * @return true if there are additional responses, false otherwise
	 */
	boolean hasNext();

	/**
	 * Specifies whether a response is available without blocking.
	 *
	 * @return true if a response is available without blocking, false otherwise
	 */
	boolean nextAvailable();

	/**
	 * Retrieves the next response, if necessary waiting until a response is available.
	 *
	 * @return a response
	 * @throws InterruptedException thrown if the thread was interrupted while waiting for the next response
	 */
	Object next() throws InterruptedException;

	/**
	 * Retrieves the next response, waiting for the specified time if there are no responses available.
	 *
	 * @param timeout how long to wait before giving up, in terms of <code>unit</code>
	 * @param unit the <code>TimeUnit</code> that <code>timeout</code> should be interpreted in
	 * 
	 * @return a response
	 * @throws TimeoutException thrown if a response wasn't available before the specified timeout
	 * @throws InterruptedException thrown if the thread was interrupted while waiting for the next response
	 */
	Object next(Long timeout, TimeUnit unit) throws TimeoutException, InterruptedException;
}
