/**
 * 
 */
package org.easycluster.easycluster.monitor.domain;

import java.util.HashMap;
import java.util.Map;

/**
 * @author wangqi
 * 
 */
public class ServerDomain {

	private String						name;

	private Map<String, ServerGroup>	groups	= new HashMap<String, ServerGroup>();

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

	public Map<String, ServerGroup> getGroups() {
		return groups;
	}

	public void setGroups(Map<String, ServerGroup> groups) {
		this.groups = groups;
	}
}
