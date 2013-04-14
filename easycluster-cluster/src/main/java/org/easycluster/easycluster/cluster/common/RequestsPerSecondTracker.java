package org.easycluster.easycluster.cluster.common;

public class RequestsPerSecondTracker {
	private int	second	= 0;
	private int	counter	= 0;
	private int	r		= 0;

	public void increase() {
		int currentSecond = (int) (System.currentTimeMillis() / 1000);
		if (second == currentSecond) {
			counter += 1;
		} else {
			second = currentSecond;
			r = counter;
			counter = 1;
		}
	}

	public int get() {
		return r;
	}
}
