package org.easycluster.easycluster.cluster;

import java.net.InetSocketAddress;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;

public class Node implements Comparable<Node> {

	private String				serviceGroup;

	private String				service;

	private String				hostName;

	private int					port		= -1;

	private String				version		= "1.0.0";

	private Integer[]			partitions	= new Integer[0];

	private String				url;

	private transient boolean	available	= false;

	public Node() {
	}

	public Node(InetSocketAddress address) {
		this(address, new Integer[0]);
	}

	public Node(InetSocketAddress address, Integer[] partitions) {
		this(address.getHostName(), address.getPort(), partitions);
	}

	public Node(String hostName, int port) {
		this(hostName, port, new Integer[0]);
	}

	public Node(String hostName, int port, Integer[] partitions) {
		this.hostName = hostName;
		this.port = port;
		this.partitions = partitions;
	}

	public String getId() {
		return getHostName() + ":" + getPort();
	}

	public String getUrl() {
		return url;
	}

	public Integer[] getPartitions() {
		return partitions;
	}

	public boolean getAvailable() {
		return available;
	}

	public void setAvailable(boolean available) {
		this.available = available;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getServiceGroup() {
		return serviceGroup;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public String getService() {
		return service;
	}

	public void setService(String service) {
		this.service = service;
	}

	public String getHostName() {
		return hostName;
	}

	public void setHostName(String hostName) {
		this.hostName = hostName;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public int getPort() {
		return port;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	@Override
	public int hashCode() {
		HashCodeBuilder builder = new HashCodeBuilder();
		builder.append(this.hostName);
		builder.append(this.port);
		return builder.toHashCode();
	}

	@Override
	public boolean equals(Object obj) {
		if (!(obj instanceof Node)) {
			return false;
		}
		Node rhs = (Node) obj;
		return new EqualsBuilder().append(hostName, rhs.hostName).append(port, rhs.port).isEquals();
	}

	@Override
	public int compareTo(Node o) {
		int val = this.hostName.compareTo(o.hostName);
		if (val == 0) {
			return this.port > o.port ? 1 : -1;
		} else {
			return val;
		}
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("Node[");
		sb.append("applicationName=[").append(serviceGroup).append("],");
		sb.append("serviceName=[").append(service).append("],");
		sb.append("hostName=[").append(hostName).append("],");
		sb.append("port=[").append(port).append("],");
		sb.append("version=[").append(version).append("],");
		sb.append("partitions=[").append(ArrayUtils.toString(partitions)).append("],");
		sb.append("url=[").append(url).append("],");
		sb.append("available=[").append(available).append("]");
		sb.append("]");
		return sb.toString();
	}
}
