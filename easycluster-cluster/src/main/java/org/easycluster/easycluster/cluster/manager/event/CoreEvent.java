package org.easycluster.easycluster.cluster.manager.event;

/**
 * The abstract event type for all events in the framework.
 * 
 */
public abstract class CoreEvent {

	private String	type;

	private String	hostName;

	private String	pid;

	private String	serviceName;

	private String	componentName;

	private String	componentType;

	public CoreEvent() {
	}

	public CoreEvent(EventType type) {
		this.type = type.name();
	}

	public CoreEvent(EventType type, String componentName, String componentType) {
		this.type = type.name();
		this.componentName = componentName;
		this.componentType = componentType;
	}

	public String getType() {
		return type;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public String getPid() {
		return pid;
	}

	public void setPid(String pid) {
		this.pid = pid;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
	}

	public String getComponentName() {
		return componentName;
	}

	public void setComponentName(String componentName) {
		this.componentName = componentName;
	}

	public String getComponentType() {
		return componentType;
	}

	public void setComponentType(String componentType) {
		this.componentType = componentType;
	}

}
