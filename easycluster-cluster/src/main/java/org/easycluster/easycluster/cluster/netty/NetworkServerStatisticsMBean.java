package org.easycluster.easycluster.cluster.netty;

public interface NetworkServerStatisticsMBean {

	double getRequestCount();

	double getRequestsPerSecond();

	double getFinishedCount();

	double getFinishedPerSecond();

	int getChannels();
}
