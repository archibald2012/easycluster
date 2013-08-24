package org.easycluster.easycluster.cluster.manager.event.handler;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Level;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.EventType;
import org.easycluster.easycluster.cluster.manager.event.LogUpdateEvent;
import org.easycluster.easycluster.core.ebus.EventBus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LogUpdateHandler {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(LogUpdateHandler.class);

	private ContextManager		contextManager	= null;

	public LogUpdateHandler(ContextManager contextManager, EventBus eventBus) {
		this.contextManager = contextManager;

		eventBus.subscribe(EventType.LOG_UPDATE.name(), this, "handleEvent");
	}

	public void handleEvent(ClusterEvent clusterEvent) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handle log update event {}", clusterEvent);
		}

		LogUpdateEvent event = (LogUpdateEvent) clusterEvent.getEvent();

		String componentName = StringUtils.trim(event.getComponentName());
		String componentType = StringUtils.trim(event.getComponentType());

		if (StringUtils.isNotBlank(componentName)) {
			Object component = contextManager.getObject(componentName, Object.class);
			if (component != null) {
				handleLogUpdateEvent(component, event);
			} else {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Cannot find target component " + componentName);
				}
			}
		} else if (StringUtils.isNotBlank(componentType)) {
			try {
				Class<? extends Object> clazz = Class.forName(componentType);
				List<? extends Object> componentList = contextManager.getObjectsByType(clazz);
				if (!componentList.isEmpty()) {
					for (Object component : componentList) {
						handleLogUpdateEvent(component, event);
					}
				} else {
					if (LOGGER.isWarnEnabled()) {
						LOGGER.warn("Cannot find target componentType " + componentType);
					}
				}
			} catch (ClassNotFoundException e) {
				LOGGER.error("Class not found.", e);
			}
		}

	}

	private void handleLogUpdateEvent(Object component, LogUpdateEvent event) {
		Level newLevel = Level.toLevel(event.getNewLevel());
		if (newLevel != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Setting logLevel to " + newLevel + " for component " + component);
			}
			updateLogLevel(component, newLevel, component.getClass());
		}
	}

	private void updateLogLevel(Object component, Level newLevel, Class<?> clazz) {
		boolean isUpdated = false;

		try {
			Field[] fields = clazz.getDeclaredFields();
			for (Field field : fields) {
				if (org.apache.log4j.Logger.class.isAssignableFrom(field.getType())) {
					field.setAccessible(true);
					org.apache.log4j.Logger logger = (org.apache.log4j.Logger) field.get(component);
					logger.setLevel(newLevel);

					if (logger.getLevel() == newLevel) {
						isUpdated = true;
					}
				}
			}
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Component " + component + " class " + clazz + " log level " + newLevel + " result " + isUpdated);
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
}
