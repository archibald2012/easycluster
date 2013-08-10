package org.easycluster.easycluster.cluster.manager.event;

public class MetricsUpdateEvent extends CoreEvent {

	private String	functionName;

	private boolean	isFilter;

	public MetricsUpdateEvent() {
		super(EventType.METRICS_UPDATE);
	}

	public MetricsUpdateEvent(String component, String clazz, String functionName, boolean isFilter) {
		super(EventType.METRICS_UPDATE, component, clazz);

		this.functionName = functionName;
		this.isFilter = isFilter;
	}

	public String getFunctionName() {
		return functionName;
	}

	public void setFunctionName(String functionName) {
		this.functionName = functionName;
	}

	public boolean isFilter() {
		return isFilter;
	}

	public void setFilter(boolean isFilter) {
		this.isFilter = isFilter;
	}

	public String toString() {
		StringBuilder builder = new StringBuilder(this.getClass().getSimpleName());

		builder.append('[').append("type=").append(getType());
		builder.append(",function=").append(functionName);
		builder.append(",isFilter=").append(isFilter).append(']');

		return builder.toString();
	}

}
