package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;


public class DefaultResponseIterator implements ResponseIterator {

	private AtomicInteger			remaining	= null;
	private BlockingQueue<Object>	responses	= new LinkedBlockingQueue<Object>();

	public DefaultResponseIterator(int numResponses) {
		remaining = new AtomicInteger(numResponses);
	}

	@Override
	public boolean hasNext() {
		return remaining.get() > 0;
	}

	@Override
	public boolean nextAvailable() {
		return responses.size() > 0;
	}

	@Override
	public Object next() throws InterruptedException {
		remaining.decrementAndGet();
		return responses.take();
	}

	@Override
	public Object next(Long timeout, TimeUnit unit) throws TimeoutException, InterruptedException {
		Object resp = responses.poll(timeout, unit);
		if (resp == null) {
			throw new TimeoutException("Timed out waiting for response");
		}
		remaining.decrementAndGet();
		return resp;
	}

	public boolean offerResponse(Object response) {
		return responses.offer(response);
	}

}
