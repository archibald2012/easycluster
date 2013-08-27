package org.easycluster.easycluster.cluster.manager.event.handler;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.easycluster.easycluster.cluster.common.SystemUtil;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.CoreEvent;
import org.easycluster.easycluster.cluster.manager.event.EventHandler;
import org.easycluster.easycluster.cluster.manager.event.EventType;
import org.easycluster.easycluster.cluster.manager.event.LogUpdateEvent;
import org.easycluster.easycluster.cluster.manager.event.MetricsUpdateEvent;
import org.easycluster.easycluster.cluster.manager.event.MessageFilterEvent;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easycluster.easycluster.core.Closure;
import org.easymetrics.easymetrics.engine.MetricsEngine;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEventHandler implements EventHandler {

	private static final Logger							LOGGER			= LoggerFactory.getLogger(DefaultEventHandler.class);

	private String										hostName		= SystemUtil.getHostName();
	private String										pid				= SystemUtil.getPid();
	private String										service			= null;
	private ContextManager								contextManager	= null;
	/**
	 * Map of event type to subscribers to the event type.
	 */
	private ConcurrentHashMap<String, List<Closure>>	subscriptions	= new ConcurrentHashMap<String, List<Closure>>();

	@Override
	public void registerObserver(String eventType, Closure closure) {
		if (eventType == null || closure == null) {
			throw new IllegalArgumentException("event [" + eventType + "], closure [" + closure + "]");
		}

		getOrCreateSubscription(eventType).add(closure);
	}

	@Override
	public void handleClusterEvent(ClusterEvent clusterEvent) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Handling cluster event ({})", clusterEvent);
		}

		CoreEvent event = clusterEvent.getEvent();

		// check the destination of this event
		boolean isTargeted = true;
		if (event.getHostName() != null) {
			if (!event.getHostName().equals(hostName)) {
				isTargeted = false;
			}
		}
		if (event.getPid() != null) {
			if (!event.getPid().equals(pid)) {
				isTargeted = false;
			}
		}
		if (event.getServiceName() != null) {
			if (!event.getServiceName().equals(service)) {
				isTargeted = false;
			}
		}
		if (!isTargeted) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Event " + event + " is not targeted to this instance.");
			}
			return;
		}

		String componentName = StringUtils.trim(event.getComponentName());
		String componentType = StringUtils.trim(event.getComponentType());

		if (StringUtils.isNotBlank(componentName)) {
			Object component = contextManager.getObject(componentName, Object.class);
			if (component != null) {
				if (isRegistered(component, event.getType())) {
					((Closure) component).execute(event);
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Component " + component + " handles event " + event);
					}
				} else {
					handleEvent(component, event);
				}
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Cannot find target component " + componentName);
				}
			}
		} else if (StringUtils.isNotBlank(componentType)) {
			try {
				Class<? extends Object> clazz = Class.forName(componentType);
				List<? extends Object> componentList = contextManager.getObjectsByType(clazz);
				if (componentList != null && !componentList.isEmpty()) {
					for (Object component : componentList) {
						if (isRegistered(component, event.getType())) {
							((Closure) component).execute(event);
							if (LOGGER.isInfoEnabled()) {
								LOGGER.info("Component " + component + " handles event " + event);
							}
						} else {
							handleEvent(component, event);
						}
					}
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Cannot find target componentType " + componentType);
					}
				}
			} catch (ClassNotFoundException e) {
				LOGGER.error("Class not found.", e);
			}
		} else {
			// it is for all components
			List<Object> componentList = contextManager.getObjectsByType(Object.class);
			if (componentList != null) {
				for (Object component : componentList) {
					if (isRegistered(component, event.getType())) {
						((Closure) component).execute(event);
						if (LOGGER.isInfoEnabled()) {
							LOGGER.info("Component " + component + " handles event " + event);
						}
					} else {
						handleEvent(component, event);
					}
				}
			}
		}

	}

	private void handleEvent(Object component, CoreEvent event) {
		if (EventType.LOG_UPDATE.name().equalsIgnoreCase(event.getType())) {
			handleLogUpdateEvent(component, (LogUpdateEvent) event);
		} else if (EventType.METRICS_UPDATE.name().equalsIgnoreCase(event.getType())) {
			handleMetricsUpdateEvent(component.getClass().getSimpleName(), (MetricsUpdateEvent) event);
		} else if (EventType.MESSAGE_FILTER.name().equalsIgnoreCase(event.getType())) {
			handleMessageFilterEvent(component, (MessageFilterEvent) event);
		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("No default handler for component " + component + " on event " + event);
			}
		}
	}

	private void handleLogUpdateEvent(Object component, LogUpdateEvent event) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling log update event for component " + component);
		}
		updateLogLevel(component, event.getNewLevel(), component.getClass());
	}

	private void handleMessageFilterEvent(Object component, MessageFilterEvent event) {
		String messageType = event.getMessageType();
		boolean isFilter = event.isFilter();
		if (NetworkServer.class.isAssignableFrom(component.getClass())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Handling message update event for component " + component);
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("MessageType '{}' isFilter '{}'", new Object[] { messageType, isFilter });
			}
			((NetworkServer) component).getMessageClosureRegistry().updateFilter(messageType, isFilter);
		}
	}

	private void handleMetricsUpdateEvent(String componentName, MetricsUpdateEvent event) {
		String functionName = event.getFunctionName();
		boolean isFilter = event.isFilter();

		List<MetricsEngine> metricsEngines = contextManager.getObjectsByType(MetricsEngine.class);
		if (metricsEngines != null) {
			for (MetricsEngine metricsEngine : metricsEngines) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Handling metrics update event for component " + metricsEngine);
				}
				boolean oldValue = metricsEngine.updateFilter(componentName, functionName, isFilter);
				if (LOGGER.isInfoEnabled()) {
					LOGGER.info("Component '{}' functionName '{}' isFilter '{}' oldValue '{}'",
							new Object[] { componentName, functionName, isFilter, oldValue });
				}
			}
		}
	}

	private void updateLogLevel(Object component, String newLevel, Class<?> clazz) {
		boolean isUpdated = false;

		try {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (org.apache.log4j.Logger.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					org.apache.log4j.Logger logger = (org.apache.log4j.Logger) field.get(component);

					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Setting logLevel to " + newLevel + " for component " + component);
					}
					Level level = Level.toLevel(newLevel);
					if (level != null) {
						logger.setLevel(level);
						if (logger.getLevel() == level) {
							isUpdated = true;
						}
					}
				} else if (java.util.logging.Logger.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					java.util.logging.Logger logger = (java.util.logging.Logger) field.get(component);
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Setting logLevel to " + newLevel + " for component " + component);
					}
					java.util.logging.Level level = java.util.logging.Level.parse(newLevel);
					if (level != null) {
						logger.setLevel(level);
						if (logger.getLevel() == level) {
							isUpdated = true;
						}
					}
				}
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Component '{}' class '{}' log level '{}' result '{}'", new Object[] { component, clazz, newLevel, isUpdated });
			}
		} catch (Exception e) {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("Failed to set " + newLevel + " on component " + component + " with error " + e.getMessage());
			}
			return;
		}
		if (clazz.getSuperclass() != null && Object.class.isAssignableFrom(clazz.getSuperclass())) {
			updateLogLevel(component, newLevel, clazz.getSuperclass());
		}
	}

	private List<Closure> getOrCreateSubscription(String event) {
		List<Closure> subscription = subscriptions.get(event);

		if (null == subscription) {
			subscription = new ArrayList<Closure>();
			subscriptions.put(event, subscription);
		}
		return subscription;
	}

	boolean isRegistered(Object component, String eventType) {
		List<Closure> components = subscriptions.get(eventType);
		return components != null && components.contains(component);
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public void setService(String service) {
		this.service = service;
	}

	public void setContextManager(ContextManager contextManager) {
		this.contextManager = contextManager;
	}

}
