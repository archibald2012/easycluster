package org.easycluster.easycluster.cluster.manager.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "functionName", "isFilter" })
public class MetricsUpdateEvent extends CoreEvent {

	@XmlTransient
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "function", required = false)
	@XmlSchemaType(name = "QName")
	private String				functionName;

	@XmlElement(name = "isFilter", required = true)
	@XmlSchemaType(name = "QName")
	private boolean				isFilter;

	public MetricsUpdateEvent() {
		super(EventType.METRICS_UPDATE.name());
	}

	public MetricsUpdateEvent(String component, Class<?> clazz, String functionName, boolean isFilter) {
		super(EventType.METRICS_UPDATE.name(), component, clazz);

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

}
