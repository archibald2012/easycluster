package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.locks.ReentrantLock;

public class AverageTracker {

	private double			total			= 0.0d;
	private long			count			= 0L;
	private double			lastTotal		= 0;
	private long			lastTimestamp	= 0;
	// per second
	private double			throughput		= 0;
	private ReentrantLock	lock			= new ReentrantLock();

	public void increase(double num) {
		lock.lock();
		try {
			total += num;
			count++;
			long now = System.currentTimeMillis();
			if (total < 0) {
				// 重置计数
				lastTotal = 0;
				total = 0;
				count = 0;
			} else {
				long interval = now - lastTimestamp;
				if (interval > 0) {// 避免除0
					throughput = (total - lastTotal) * 1000 / interval;
					lastTotal = total;
				}
			}
			lastTimestamp = now;
		} finally {
			lock.unlock();
		}
	}

	public double getTotal() {
		return total;
	}

	public double getAverage() {
		return count == 0 ? 0 : total / count;
	}

	public double getThroughput() {
		return throughput;
	}

}
