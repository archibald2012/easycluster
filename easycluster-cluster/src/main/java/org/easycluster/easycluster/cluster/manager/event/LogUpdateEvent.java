package org.easycluster.easycluster.cluster.manager.event;

public class LogUpdateEvent extends CoreEvent {

	private String	newLevel;

	public LogUpdateEvent() {
		super(EventType.LOG_UPDATE);
	}

	public LogUpdateEvent(String component, String clazz, String newLevel) {
		super(EventType.LOG_UPDATE, component, clazz);

		this.newLevel = newLevel;
	}

	public String getNewLevel() {
		return newLevel;
	}

	public void setNewLevel(String newLevel) {
		this.newLevel = newLevel;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());

		builder.append('[').append("type=").append(getType());
		builder.append(",newLevel=").append(newLevel).append(']');

		return builder.toString();
	}

}
