package org.easycluster.easycluster.monitor.domain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.easycluster.easycluster.core.IpPortPair;

/**
 * @author wangqi
 * 
 */
public class ServerGroup {

	private String																		name;

	// key is server instance id
	private ConcurrentMap<IpPortPair, ServerStatus>	servers	= new ConcurrentHashMap<IpPortPair, ServerStatus>();

	public ServerGroup(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConcurrentMap<IpPortPair, ServerStatus> getServers() {
		return servers;
	}

	public void setServers(ConcurrentMap<IpPortPair, ServerStatus> servers) {
		this.servers = servers;
	}
}
