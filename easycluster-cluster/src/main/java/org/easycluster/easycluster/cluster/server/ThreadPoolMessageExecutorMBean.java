package org.easycluster.easycluster.cluster.server;

public interface ThreadPoolMessageExecutorMBean {

	int getQueueSize();

	long getAverageWaitTime();

	long getAverageProcessingTime();

	long getRequestCount();
}
