package org.easycluster.easycluster.cluster.netty;

public interface NetworkServerStatisticsMBean {

	long getRequestCount();

	int getRequestsPerSecond();

	long getFinishedCount();

	int getFinishedPerSecond();

	int getChannels();
}
