/**
 * 
 */
package org.easycluster.easycluster.monitor.domain;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

/**
 * @author wangqi
 * 
 */
public class ServerStatus implements Comparable<ServerStatus> {

	private String	ip;

	private int		port;

	private String	version;

	private String	domain;

	private String	group;

	private boolean	available;

	private long	heartbeatTime;

	private long	startTime;

	public ServerStatus() {
		this.startTime = System.currentTimeMillis();
		this.heartbeatTime = System.currentTimeMillis();
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getGroup() {
		return group;
	}

	public void setGroup(String group) {
		this.group = group;
	}

	public boolean isAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public long getHeartbeatTime() {
		return heartbeatTime;
	}

	public void setHeartbeatTime(long heartbeatTime) {
		this.heartbeatTime = heartbeatTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public long getRunningTime() {
		return available ? Math.abs(getNumberOfSecondsBetween(heartbeatTime, startTime)) : 0;
	}

	private int getNumberOfSecondsBetween(final double d1, final double d2) {
		if ((d1 == 0) || (d2 == 0)) {
			return -1;
		}

		return (int) (Math.abs(d1 - d2) / 1000);
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

	public int compareTo(ServerStatus o) {
		int rslt = this.getIp().compareTo(o.getIp());
		if (0 == rslt) {
			return this.getPort() - o.getPort();
		}
		return rslt;
	}
}
