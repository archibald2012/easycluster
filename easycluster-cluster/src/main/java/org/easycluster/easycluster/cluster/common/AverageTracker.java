package org.easycluster.easycluster.cluster.common;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

public class AverageTracker {

	private BlockingQueue<Long>	q	= null;
	private int					size;

	public AverageTracker(int size) {
		this.size = size;
		q = new LinkedBlockingQueue<Long>();
	}

	public void add(long num) {
		q.add(num);
		if (q.size() > size) {
			q.poll();
		}
	}

	public double getAverage() {
		List<Long> list = new ArrayList<Long>();
		q.drainTo(list);
		long sum = 0;
		for (Long num : list) {
			sum += num;
		}
		return (list.size() > 0) ? (double) sum / list.size() : sum;
	}

}
