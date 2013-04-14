package org.easycluster.easycluster.cluster.common;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

public class NamedPoolThreadFactory implements ThreadFactory {

	private AtomicInteger	threadCount	= new AtomicInteger(1);
	private String			poolName	= "";
	private String			nameFormat	= "%s-thread-%d";

	public NamedPoolThreadFactory(String poolName) {
		this.poolName = poolName;
	}

	@Override
	public Thread newThread(Runnable r) {
		return new Thread(Thread.currentThread().getThreadGroup(), r, String.format(nameFormat, poolName,
				threadCount.getAndIncrement()));
	}

	public String getPoolName() {
		return poolName;
	}

	public int getThreadCount() {
		return threadCount.get();
	}

}
