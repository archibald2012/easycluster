package org.easycluster.easycluster.cluster.netty;

public interface NetworkClientStatisticsMBean {
	int getRequestsPerSecond();

	long getAverageRequestProcessingTime();
}
