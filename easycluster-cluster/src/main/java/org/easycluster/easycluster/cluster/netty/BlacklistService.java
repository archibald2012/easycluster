package org.easycluster.easycluster.cluster.netty;

import org.apache.commons.lang.StringUtils;

public class BlacklistService {

	private String[]	ips;
	private String[]	users;

	public void setBlackList(String configInfo) {
		int index = configInfo.indexOf("|");
		if (index < 0) {
			return;
		}
		String ipStr = configInfo.substring(0, index);
		String userStr = configInfo.substring(index + 1);
		ips = ipStr.split(",");
		users = userStr.split(",");
	}

	public boolean existIp(String ip) {
		String[] ips = this.ips;
		if (ips != null && StringUtils.isNotBlank(ip)) {
			for (String item : ips) {
				if (ip.equals(item)) {
					return true;
				}
			}
		}

		return false;
	}

	public boolean existUserId(String uid) {
		String[] users = this.users;
		if (users != null && StringUtils.isNotBlank(uid)) {
			for (String item : users) {
				if (uid.equals(item)) {
					return true;
				}
			}
		}

		return false;
	}

}
