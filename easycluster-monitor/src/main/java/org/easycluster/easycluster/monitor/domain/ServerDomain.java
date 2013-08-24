/**
 * 
 */
package org.easycluster.easycluster.monitor.domain;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * @author wangqi
 * 
 */
public class ServerDomain {

	private String name;

	private ConcurrentMap<String, ServerGroup> groups = new ConcurrentHashMap<String, ServerGroup>();

	public ServerDomain(String name) {
		this.name = name;
	}

	public ServerGroup getGroup(String groupName) {
		return groups.get(groupName);
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public ConcurrentMap<String, ServerGroup> getGroups() {
		return groups;
	}

	public void setGroups(ConcurrentMap<String, ServerGroup> groups) {
		this.groups = groups;
	}
}
