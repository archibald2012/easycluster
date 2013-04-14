package org.easycluster.easycluster.cluster.manager.event;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;


/**
 * XML document container for events.
 * 
 */
@XmlRootElement(name = "ClusterEvent")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ClusterEvent", propOrder = { "event" })
public class ClusterEvent implements Serializable {
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "event", required = true)
	private CoreEvent					event;

	public ClusterEvent() {
	}

	public ClusterEvent(CoreEvent event) {
		this.event = event;
	}

	public CoreEvent getEvent() {
		return event;
	}

	public void setEvent(CoreEvent event) {
		this.event = event;
	}

}
