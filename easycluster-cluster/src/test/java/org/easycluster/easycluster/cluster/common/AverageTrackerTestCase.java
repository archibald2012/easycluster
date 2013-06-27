package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.CountDownLatch;

import org.junit.Test;

public class AverageTrackerTestCase {

	@Test
	public void testAverage() throws Exception {
		final AverageTracker at = new AverageTracker();

		final int num = 1000;
		final CountDownLatch latch = new CountDownLatch(num);
		new Thread() {
			public void run() {
				for (int i = 0; i < num; i++) {
					at.increase(i);
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
			System.out.println("Throughput: " + at.getThroughput());
			System.out.println("Total: " + at.getTotal());

			Thread.sleep(1);
			if (latch.getCount() == 0) {
				break;
			}
		}

		latch.await();
	}
	
	@Test
	public void testIncrease() throws Exception {
		final AverageTracker tracker = new AverageTracker();
		final int num = 10000;

		final CountDownLatch latch = new CountDownLatch(num);

		new Thread() {
			public void run() {

				for (int i = 0; i < num; i++) {
					tracker.increase(1);
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

		new Thread() {
			public void run() {
				while (latch.getCount() > 0) {
					System.out.println("Throughput: " + tracker.getThroughput());

					try {
						Thread.sleep(1);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		}.start();
		latch.await();

	}
}
