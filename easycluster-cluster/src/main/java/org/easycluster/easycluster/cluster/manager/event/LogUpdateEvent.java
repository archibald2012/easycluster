package org.easycluster.easycluster.cluster.manager.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "newLevel" })
public class LogUpdateEvent extends CoreEvent {

	@XmlTransient
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "newLevel", required = true)
	@XmlSchemaType(name = "QName")
	private String				newLevel;

	public LogUpdateEvent() {
		super(EventType.LOG_UPDATE.name());
	}

	public LogUpdateEvent(String component, Class<?> clazz, String newLevel) {
		super(EventType.LOG_UPDATE.name(), component, clazz);

		this.newLevel = newLevel;
	}

	public String getNewLevel() {
		return newLevel;
	}

	public void setNewLevel(String newLevel) {
		this.newLevel = newLevel;
	}

}
