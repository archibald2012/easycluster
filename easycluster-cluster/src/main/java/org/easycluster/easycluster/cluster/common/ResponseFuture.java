package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class ResponseFuture implements Future<Object> {

	private CountDownLatch	latch		= new CountDownLatch(1);
	private volatile Object	response	= null;

	@Override
	public boolean cancel(boolean mayInterruptIfRunning) {
		return false;
	}

	@Override
	public boolean isCancelled() {
		return false;
	}

	@Override
	public boolean isDone() {
		return latch.getCount() == 0;
	}

	@Override
	public Object get() throws InterruptedException, ExecutionException {
		latch.await();
		return response;
	}

	@Override
	public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
		latch.await(timeout, unit);
		if (response == null) {
			throw new TimeoutException("Timed out waiting for response");
		}

		return response;
	}

	public void offerResponse(Object resp) {
		response = resp;
		latch.countDown();
	}

}
