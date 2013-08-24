package org.easycluster.easycluster.monitor.domain;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import org.easycluster.easycluster.core.IpPortPair;

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

		IpPortPair key = new IpPortPair(server.getIp(), server.getPort());
		ServerStatus snapshot = group.getServers().get(key);
		if (snapshot == null) {
			snapshot = new ServerStatus();
		} else {
			// recover
			if (!snapshot.isAvailable() && server.isAvailable()) {
				snapshot = new ServerStatus();
			}
		}

		snapshot.setDomain(server.getDomain());
		snapshot.setGroup(server.getGroup());
		snapshot.setIp(server.getIp());
		snapshot.setPort(server.getPort());
		snapshot.setVersion(server.getVersion());
		snapshot.setAvailable(server.isAvailable());
		if (server.isAvailable()) {
			snapshot.setHeartbeatTime(System.currentTimeMillis());
		}

		group.getServers().put(key, snapshot);
	}

	public ServerDomain[] getDomainByAll() {
		return domains.values().toArray(new ServerDomain[] {});
	}

	public ServerStatus[] getServerSnapshot(String domain, String groupName) {
		List<ServerStatus> ret = new ArrayList<ServerStatus>();
		ServerDomain serverDomain = domains.get(domain);
		if (serverDomain == null) {
			return ret.toArray(new ServerStatus[0]);
		}
		ServerGroup group = serverDomain.getGroup(groupName);
		if (group == null) {
			return ret.toArray(new ServerStatus[0]);
		} else {
			ret.addAll(group.getServers().values());
			Collections.sort(ret);
			return ret.toArray(new ServerStatus[0]);
		}
	}

	public ServerStatus getServerSnapshotByAddress(IpPortPair address) {
		ServerStatus ret = null;

		Collection<ServerDomain> domains = this.domains.values();
		for (ServerDomain domain : domains) {
			for (ServerGroup group : domain.getGroups().values()) {
				if (group.getServers().containsKey(address)) {
					return group.getServers().get(address);
				}
			}
		}
		return ret;
	}

	public ServerStatus[] getServerSnapshotByDomain(String domainName) {
		List<ServerStatus> ret = new ArrayList<ServerStatus>();

		ServerDomain domain = domains.get(domainName);
		for (ServerGroup group : domain.getGroups().values()) {
			for (ServerStatus snapshot : group.getServers().values()) {
				ret.add(snapshot);
			}
		}
		Collections.sort(ret);
		return ret.toArray(new ServerStatus[0]);
	}

	public ServerStatus[] getServerSnapshotByAll() {
		List<ServerStatus> ret = new ArrayList<ServerStatus>();

		for (ServerDomain domain : domains.values()) {
			for (ServerGroup group : domain.getGroups().values()) {
				for (ServerStatus snapshot : group.getServers().values()) {
					ret.add(snapshot);
				}
			}
		}
		Collections.sort(ret);
		return ret.toArray(new ServerStatus[0]);
	}
}
