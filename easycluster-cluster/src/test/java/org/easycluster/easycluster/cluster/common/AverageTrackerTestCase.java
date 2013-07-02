package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class AverageTrackerTestCase {

	@Test
	public void testAverage() throws Exception {
		final AverageTracker at = new AverageTracker(100);

		final int num = 1000;
		final CountDownLatch latch = new CountDownLatch(num);
		new Thread() {
			public void run() {
				for (int i = 0; i < num; i++) {
					at.add(i);
					latch.countDown();
					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();

		while (true) {
			System.out.println("Average: " + at.getAverage());

			Thread.sleep(1);
			if (latch.getCount() == 0) {
				break;
			}
		}

		latch.await();
	}

	
}
