package org.easycluster.easycluster.core.ebus;

import java.util.concurrent.atomic.AtomicInteger;

public class SampleEventUnit {

	private AtomicInteger	count	= new AtomicInteger(0);

	public void increase() {
		count.incrementAndGet();
	}

	public void increase2() {
		count.incrementAndGet();
	}

	public void increaseByStep(int delta) {
		count.addAndGet(delta);
	}

	public void increaseBySignal(SampleSignal signal) {
		count.addAndGet(signal.getIntField());
	}

	public AtomicInteger getCount() {
		return count;
	}

}
