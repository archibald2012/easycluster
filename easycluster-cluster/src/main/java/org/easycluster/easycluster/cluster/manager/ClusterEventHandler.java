package org.easycluster.easycluster.cluster.manager;

import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.core.ebus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class ClusterEventHandler {

	private static final Logger	LOGGER		= LoggerFactory.getLogger(ClusterEventHandler.class);

	private EventBus						eventBus	= null;

	public ClusterEventHandler(EventBus eventBus) {
		this.eventBus = eventBus;
	}

	public void handleClusterEvent(ClusterEvent clusterEvent) {
		LOGGER.info("Handling cluster event ({})", clusterEvent);

		eventBus.publish(clusterEvent.getEvent().getType(), clusterEvent);
	}
}
