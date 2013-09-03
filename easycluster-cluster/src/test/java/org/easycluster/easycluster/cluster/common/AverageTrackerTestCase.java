package org.easycluster.easycluster.cluster.common;

import static org.junit.Assert.*;
import java.util.concurrent.CountDownLatch;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class AverageTrackerTestCase {

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testAverage() throws Exception {
		final AverageTracker at = new AverageTracker(100);

		final int num = 100;
		final CountDownLatch latch = new CountDownLatch(num);
		new Thread() {
			public void run() {
				for (int i = 1; i <= num; i++) {
					at.add(i);
					latch.countDown();
					try {
						Thread.sleep(10);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
				}
			}
		}.start();

		latch.await();
		assertEquals(50.5d, at.getAverage(), 0);

	}
}
