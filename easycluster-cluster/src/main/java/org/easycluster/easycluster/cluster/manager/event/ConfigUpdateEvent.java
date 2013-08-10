package org.easycluster.easycluster.cluster.manager.event;

import java.util.Map;

public class ConfigUpdateEvent extends CoreEvent {

	private Map<String, String>	newConfig;

	public ConfigUpdateEvent() {
		super(EventType.CONFIG_UPDATE);
	}

	public ConfigUpdateEvent(String component, String clazz, Map<String, String> newConfig) {
		super(EventType.CONFIG_UPDATE, component, clazz);

		this.newConfig = newConfig;
	}

	public Map<String, String> getNewConfig() {
		return newConfig;
	}

	public void setNewConfig(Map<String, String> newConfig) {
		this.newConfig = newConfig;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());

		builder.append('[').append("type=").append(getType());
		builder.append(",newConfig=").append(newConfig).append(']');

		return builder.toString();
	}

}
