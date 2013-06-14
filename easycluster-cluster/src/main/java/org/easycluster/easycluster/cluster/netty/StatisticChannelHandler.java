package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.cluster.common.AverageTimeTracker;
import org.easycluster.easycluster.cluster.common.RequestsPerSecondTracker;
import org.jboss.netty.channel.SimpleChannelHandler;

public class StatisticChannelHandler extends SimpleChannelHandler {

	private AverageTimeTracker			processingTime	= new AverageTimeTracker(100);
	private RequestsPerSecondTracker	rps				= new RequestsPerSecondTracker();

	class NetworkStatisticsMBean {
		public int getRequestsPerSecond() {
			return rps.get();
		}

		public long getAverageRequestProcessingTime() {
			return processingTime.average();
		}
	}
}
