package org.easycluster.easycluster.cluster.manager.event;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlSeeAlso;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * The abstract event type for all events in the framework.
 * 
 */
@XmlSeeAlso({ LogUpdateEvent.class, MetricsUpdateEvent.class, MessageFilterEvent.class })
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "type", "hostName", "pid", "serviceName", "componentName", "componentType" })
public abstract class CoreEvent implements Serializable {

	@XmlTransient
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "type", required = true)
	@XmlSchemaType(name = "QName")
	private String				type;

	@XmlElement(name = "host", required = false)
	@XmlSchemaType(name = "QName")
	private String				hostName;

	@XmlElement(name = "pid", required = false)
	@XmlSchemaType(name = "QName")
	private String				pid;

	@XmlElement(name = "service", required = false)
	@XmlSchemaType(name = "QName")
	private String				serviceName;

	@XmlElement(name = "component", required = false)
	@XmlSchemaType(name = "QName")
	private String				componentName;

	@XmlElement(name = "componentType", required = false)
	@XmlSchemaType(name = "QName")
	private String				componentType;

	public CoreEvent() {
	}

	public CoreEvent(String type) {
		this.type = type;
	}

	public CoreEvent(String type, String componentName, Class<?> componentType) {
		this.type = type;
		this.componentName = componentName;
		this.componentType = componentType != null ? componentType.getName() : null;
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

	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
