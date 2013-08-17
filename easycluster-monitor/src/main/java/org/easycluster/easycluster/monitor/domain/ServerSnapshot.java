/**
 * 
 */
package org.easycluster.easycluster.monitor.domain;

import java.util.Date;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

public class ServerSnapshot implements Comparable<ServerSnapshot> {

	private ServerStatus	serverStatus;

	private long			heartbeatTime;

	private long			starttime;

	public ServerSnapshot() {
		this.heartbeatTime = System.currentTimeMillis();
		this.starttime = heartbeatTime;
	}

	public ServerStatus getServerStatus() {
		return serverStatus;
	}

	public void setServerStatus(ServerStatus serverStatus) {
		this.serverStatus = serverStatus;
	}

	public Date getStarttime() {
		return new Date(starttime);
	}

	public long getRunningTime() {
		return Math.abs(getNumberOfSecondsBetween(heartbeatTime, starttime));
	}

	public void setStarttime(long starttime) {
		this.starttime = starttime;
	}

	public void setHeartbeatTime(long heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public long getHeartbeatTime() {
		return heartbeatTime;
	}

	public boolean isRunning() {
		return (System.currentTimeMillis() - heartbeatTime) <= 20 * 1000;
	}

	private int getNumberOfSecondsBetween(final double d1, final double d2) {
		if ((d1 == 0) || (d2 == 0)) {
			return -1;
		}

		return (int) (Math.abs(d1 - d2) / 1000);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
	}

	public int compareTo(ServerSnapshot o) {
		int rslt = this.serverStatus.getIp().compareTo(o.serverStatus.getIp());
		if (0 == rslt) {
			return this.serverStatus.getPort() - o.serverStatus.getPort();
		}
		return rslt;
	}
}
