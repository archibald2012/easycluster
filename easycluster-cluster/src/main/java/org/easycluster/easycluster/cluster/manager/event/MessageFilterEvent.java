package org.easycluster.easycluster.cluster.manager.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "messageType", "isFilter" })
public class MessageFilterEvent extends CoreEvent {

	@XmlTransient
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "message", required = false)
	@XmlSchemaType(name = "QName")
	private String				messageType;

	@XmlElement(name = "isFilter", required = true)
	@XmlSchemaType(name = "QName")
	private boolean				isFilter;

	public MessageFilterEvent() {
		super(EventType.MESSAGE_FILTER.name());
	}

	public MessageFilterEvent(String component, Class<?> clazz, Class<?> messageType, boolean isFilter) {
		super(EventType.MESSAGE_FILTER.name(), component, clazz);

		this.messageType = messageType.getName();
		this.isFilter = isFilter;
	}

	public String getMessageType() {
		return messageType;
	}

	public void setMessageType(String messageType) {
		this.messageType = messageType;
	}

	public boolean isFilter() {
		return isFilter;
	}

	public void setFilter(boolean isFilter) {
		this.isFilter = isFilter;
	}

}
