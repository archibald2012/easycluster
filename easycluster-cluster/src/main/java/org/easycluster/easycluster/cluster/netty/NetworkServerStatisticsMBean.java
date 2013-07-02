package org.easycluster.easycluster.cluster.netty;

public interface NetworkServerStatisticsMBean {

	long getRequestCount();

	double getRequestsPerSecond();

	long getFinishedCount();

	double getFinishedPerSecond();

	int getChannels();
}
