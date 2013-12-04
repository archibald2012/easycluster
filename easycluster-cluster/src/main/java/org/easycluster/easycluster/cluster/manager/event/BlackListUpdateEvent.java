package org.easycluster.easycluster.cluster.manager.event;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlSchemaType;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(propOrder = { "ips", "users" })
public class BlackListUpdateEvent extends CoreEvent {

	@XmlTransient
	private static final long	serialVersionUID	= 1L;

	@XmlElement(name = "ips", required = false)
	@XmlSchemaType(name = "QName")
	private String				ips;

	@XmlElement(name = "users", required = false)
	@XmlSchemaType(name = "QName")
	private String				users;

	public BlackListUpdateEvent() {
		super(EventType.BLACKLIST_UPDATE.name());
	}

	public String getIps() {
		return ips;
	}

	public void setIps(String ips) {
		this.ips = ips;
	}

	public String getUsers() {
		return users;
	}

	public void setUsers(String users) {
		this.users = users;
	}

}
