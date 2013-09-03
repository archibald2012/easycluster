package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import junit.framework.Assert;

import org.junit.Test;

public class ResponseFutureTestCase {

	@Test
	public void testOfferResponse() throws Exception {
		final ResponseFuture future = new ResponseFuture();

		final String resp = "hello";
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				future.offerResponse(resp);
			}
		}).start();

		Assert.assertFalse(future.isDone());

		Object o = future.get();

		Assert.assertTrue(future.isDone());
		Assert.assertFalse(future.isCancelled());
		Assert.assertEquals(resp, o);
	}

	@Test(expected = TimeoutException.class)
	public void testOfferResponse_timeout() throws Exception {
		final ResponseFuture future = new ResponseFuture();

		Assert.assertFalse(future.isDone());

		future.get(100, TimeUnit.MILLISECONDS);

	}

}
