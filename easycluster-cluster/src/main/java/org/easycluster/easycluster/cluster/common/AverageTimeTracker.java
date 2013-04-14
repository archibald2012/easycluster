package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AverageTimeTracker {

	private int					size	= 0;
	private BlockingQueue<Long>	q		= null;
	private long				sum		= 0;

	public AverageTimeTracker(int size) {
		this.size = size;
		q = new LinkedBlockingQueue<Long>();
	}

	public void add(long time) {
		addTime(time);
	}

	public void addTime(long time) {
		q.add(time);
		long old = (q.size() > size) ? q.poll() : 0;
		sum = (sum - old + time);
	}

	public long average() {
		return (q.size() > 0) ? sum / q.size() : sum;
	}
}
