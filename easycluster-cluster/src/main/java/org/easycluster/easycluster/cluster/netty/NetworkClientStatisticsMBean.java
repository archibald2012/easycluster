package org.easycluster.easycluster.cluster.netty;

public interface NetworkClientStatisticsMBean {
	double getRequestsPerSecond();

	double getAverageRequestProcessingTime();
}
