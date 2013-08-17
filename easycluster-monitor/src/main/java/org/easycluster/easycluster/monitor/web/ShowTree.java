package org.easycluster.easycluster.monitor.web;

import java.util.Map;

import org.easycluster.easycluster.monitor.domain.ServerDomain;
import org.easycluster.easycluster.monitor.domain.ServerSnapshot;
import org.easycluster.easycluster.monitor.service.ServerStore;

/**
 * @author wangqi
 * 
 */
public class ShowTree {

	private ServerStore	serverStore;

	public ServerDomain[] showAll() {
		ServerDomain[] s = serverStore.getDomainByAll();
		return s;
	}

	public ServerSnapshot[] showByDomain(String domain) {
		ServerSnapshot[] s = serverStore.getServerSnapshotByDomain(domain);
		return s;
	}

	public ServerSnapshot[] showByGroup(String domain, String group) {
		ServerSnapshot[] s = serverStore.getServerSnapshot(domain, group);
		return s;
	}

	public ListRange showServerSnapshot(Map<String, Object> params) {
		ServerSnapshot[] s;
		if (params.get("group") != null) {
			s = serverStore.getServerSnapshot(params.get("domain").toString(), params.get("group").toString());
		} else if (params.get("domain") != null) {
			s = serverStore.getServerSnapshotByDomain(params.get("domain").toString());
		} else {
			s = serverStore.getServerSnapshotByAll();
		}
		ListRange lr = new ListRange();
		lr.setData(s);
		lr.setTotalSize(s.length);
		return lr;
	}

	public void setServerStore(ServerStore serverStore) {
		this.serverStore = serverStore;
	}
	
}
