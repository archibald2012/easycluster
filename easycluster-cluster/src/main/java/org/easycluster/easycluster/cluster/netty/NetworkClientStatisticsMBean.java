package org.easycluster.easycluster.cluster.netty;

public interface NetworkClientStatisticsMBean {
	double getFinishedPerSecond();

	double getAverageRequestProcessingTime();
}
