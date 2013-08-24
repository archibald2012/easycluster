package org.easycluster.easycluster.cluster.manager.event.handler;

import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.EventType;
import org.easycluster.easycluster.cluster.manager.event.MetricsUpdateEvent;
import org.easycluster.easycluster.core.ebus.EventBus;
import org.easymetrics.easymetrics.engine.MetricsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MetricsUpdateHandler {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(MetricsUpdateHandler.class);

	private ContextManager		contextManager	= null;

	public MetricsUpdateHandler(ContextManager contextManager, EventBus eventBus) {
		this.contextManager = contextManager;

		eventBus.subscribe(EventType.METRICS_UPDATE.name(), this, "handleEvent");
	}

	public void handleEvent(ClusterEvent clusterEvent) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handle metrics update event {}", clusterEvent);
		}

		MetricsUpdateEvent event = (MetricsUpdateEvent) clusterEvent.getEvent();

		String componentName = StringUtils.trim(event.getComponentName());
		String componentType = StringUtils.trim(event.getComponentType());

		if (StringUtils.isNotBlank(componentName)) {
			Object component = contextManager.getObject(componentName, Object.class);
			if (component != null) {
				handleMetricsUpdateEvent(componentName, (MetricsUpdateEvent) event);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Cannot find target component " + componentName);
				}
			}
		} else if (StringUtils.isNotBlank(componentType)) {
			try {
				Class<? extends Object> clazz = Class.forName(componentType);
				handleMetricsUpdateEvent(clazz.getName(), event);
			} catch (ClassNotFoundException e) {
				LOGGER.error("Class not found.", e);
			}
		}
		
	}

	private void handleMetricsUpdateEvent(String componentName, MetricsUpdateEvent event) {
		String functionName = event.getFunctionName();
		boolean isFilter = event.isFilter();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling metrics event " + event + " for component " + componentName);
		}

		List<MetricsEngine> metricsEngines = contextManager.getObjectsByType(MetricsEngine.class);
		for (MetricsEngine metricsEngine : metricsEngines) {
			metricsEngine.updateFilter(componentName, functionName, isFilter);
		}
	}
}
