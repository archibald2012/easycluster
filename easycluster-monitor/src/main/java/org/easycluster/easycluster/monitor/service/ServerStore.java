package org.easycluster.easycluster.monitor.service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.easycluster.easycluster.core.IpPortPair;
import org.easycluster.easycluster.monitor.domain.ServerDomain;
import org.easycluster.easycluster.monitor.domain.ServerGroup;
import org.easycluster.easycluster.monitor.domain.ServerSnapshot;
import org.easycluster.easycluster.monitor.domain.ServerStatus;

/**
 * @author wangqi
 * 
 */
public class ServerStore {
	
	private ConcurrentMap<String, ServerDomain>	domains	= new ConcurrentHashMap<String, ServerDomain>();

	public void refreshServers(String domainName, String groupName, ServerStatus server) {

		ServerDomain domain = domains.get(domainName);
		if (domain == null) {
			domain = new ServerDomain(domainName);
			domains.put(domainName, domain);
		}

		ServerGroup group = domain.getGroup(groupName);
		if (group == null) {
			group = new ServerGroup(groupName);
			domain.getGroups().put(groupName, group);
		}

		ServerSnapshot snapshot = null;
		IpPortPair key = new IpPortPair(server.getIp(), server.getPort());
		if (group.getServers().containsKey(key)) {

			snapshot = group.getServers().get(key);
			long now = System.currentTimeMillis();
			snapshot.setHeartbeatTime(now);
		} else {
			snapshot = new ServerSnapshot();
		}

		snapshot.setServerStatus(server);
		group.getServers().put(key, snapshot);
	}

	public ServerDomain[] getDomainByAll() {
		return domains.values().toArray(new ServerDomain[] {});
	}
	
	public ServerSnapshot[] getServerSnapshot(String domain, String groupName) {
		List<ServerSnapshot> ret = new ArrayList<ServerSnapshot>();
		ServerDomain serverDomain = domains.get(domain);
		if (serverDomain == null) {
			return ret.toArray(new ServerSnapshot[0]);
		}
		ServerGroup group = serverDomain.getGroup(groupName);
		if (group == null) {
			return ret.toArray(new ServerSnapshot[0]);
		} else {
			ret.addAll(group.getServers().values());
			Collections.sort(ret);
			return ret.toArray(new ServerSnapshot[0]);
		}
	}

	public ServerSnapshot getServerSnapshotByAddress(IpPortPair address) {
		ServerSnapshot ret = null;

		Collection<ServerDomain> domains = this.domains.values();
		for (ServerDomain domain : domains) {
			for (ServerGroup group : domain.getGroups().values()) {
				for (ServerSnapshot snapshot : group.getServers().values()) {
					IpPortPair server = new IpPortPair(snapshot.getServerStatus().getIp(), snapshot.getServerStatus().getPort());
					if (server.equals(address)) {
						ret = snapshot;
						break;
					}
				}
			}
		}
		return ret;
	}

	public ServerSnapshot[] getServerSnapshotByDomain(String domainName) {
		List<ServerSnapshot> ret = new ArrayList<ServerSnapshot>();

		ServerDomain domain = domains.get(domainName);
		for (ServerGroup group : domain.getGroups().values()) {
			for (ServerSnapshot snapshot : group.getServers().values()) {
				ret.add(snapshot);
			}
		}
		Collections.sort(ret);
		return ret.toArray(new ServerSnapshot[0]);
	}

	public ServerSnapshot[] getServerSnapshotByAll() {
		List<ServerSnapshot> ret = new ArrayList<ServerSnapshot>();

		for (ServerDomain domain : domains.values()) {
			for (ServerGroup group : domain.getGroups().values()) {
				for (ServerSnapshot snapshot : group.getServers().values()) {
					ret.add(snapshot);
				}
			}
		}
		Collections.sort(ret);
		return ret.toArray(new ServerSnapshot[0]);
	}
}
