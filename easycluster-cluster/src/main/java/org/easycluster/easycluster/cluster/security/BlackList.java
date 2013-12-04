package org.easycluster.easycluster.cluster.security;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.cluster.manager.event.BlackListUpdateEvent;
import org.easycluster.easycluster.cluster.manager.event.EventHandler;
import org.easycluster.easycluster.cluster.manager.event.EventType;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BlackList implements Closure {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(BlackList.class);

	private volatile String[]	ips		= new String[0];
	private volatile String[]	users	= new String[0];

	public BlackList(String configInfo, EventHandler eventHandler) {

		udpateConfig(configInfo);
		
		if (eventHandler != null) {
			eventHandler.registerObserver(EventType.BLACKLIST_UPDATE.name(), this);
		}
	}

	@Override
	public void execute(Object msg) {
		BlackListUpdateEvent event = (BlackListUpdateEvent) msg;
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling the black list update event [{}]", event);
		}

		if (event.getIps() != null) {
			ips = event.getIps().split(",");
		}
		if (event.getUsers() != null) {
			users = event.getUsers().split(",");
		}
	}

	public boolean containsIp(String ip) {
		return ArrayUtils.contains(ips, ip);
	}

	public boolean containsUser(String uid) {
		return ArrayUtils.contains(users, uid);
	}

	private void udpateConfig(String configInfo) {
		int index = configInfo.indexOf("|");

		String ipStr = (index > 0) ? configInfo.substring(0, index) : configInfo;
		ips = ipStr.split(",");
		if (index > 0) {
			String userStr = configInfo.substring(index + 1);
			users = userStr.split(",");
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("BlackList[");
		sb.append("ips=[").append(ArrayUtils.toString(ips)).append("],");
		sb.append("users=[").append(ArrayUtils.toString(users)).append("]");
		sb.append("]");
		return sb.toString();
	}

}
