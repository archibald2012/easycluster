package org.easycluster.easycluster.cluster.server;

public interface ThreadPoolMessageExecutorMBean {

	int getQueueSize();

	int getCorePoolSize();

	int getMaxPoolSize();

	int getLargestPoolSize();

	int getPoolSize();

	int getActiveCount();

	double getAverageWaitTime();

	long getKeepAliveTime();

	double getAverageProcessingTime();

	long getRequestCount();

	long getCompletedTaskCount();
}
