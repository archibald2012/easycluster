package org.easycluster.easycluster.cluster.netty;

public interface ChannelPoolMBean {

	int getOpenChannels();

	int getMaxChannels();

	int getWriteQueueSize();

	int getNumberRequestsSent();
}
