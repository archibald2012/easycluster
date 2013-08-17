package org.easycluster.easycluster.monitor.domain;

import java.util.HashMap;
import java.util.Map;

import org.easycluster.easycluster.core.IpPortPair;

/**
 * @author wangqi
 * 
 */
public class ServerGroup {

	private String							name;

	private Map<IpPortPair, ServerSnapshot>	servers	= new HashMap<IpPortPair, ServerSnapshot>();

	public ServerGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Map<IpPortPair, ServerSnapshot> getServers() {
		return servers;
	}

	public void setServers(Map<IpPortPair, ServerSnapshot> servers) {
		this.servers = servers;
	}
}
