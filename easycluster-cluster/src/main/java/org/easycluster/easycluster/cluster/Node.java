package org.easycluster.easycluster.cluster;

import java.net.InetSocketAddress;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

@XmlRootElement(name = "node")
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Node", propOrder = { "applicationName", "serviceName", "hostName", "port", "version", "partitions", "url" })
public class Node implements Comparable<Node> {

	@XmlElement(name = "application", required = true)
	private String				applicationName;

	@XmlElement(name = "service", required = true)
	private String				serviceName;

	@XmlElement(name = "host", required = true)
	private String				hostName;

	@XmlElement(name = "port", required = true)
	private int					port		= -1;

	@XmlElement(name = "version", required = true)
	private String				version		= "1.0.0";

	@XmlElement(name = "partitions", required = false)
	private int[]				partitions	= new int[0];

	@XmlElement(name = "url", required = false)
	private String				url;

	private transient boolean	available	= false;

	public Node() {
	}

	public Node(InetSocketAddress address, boolean available) {
		this(address, new int[0], available);
	}

	public Node(InetSocketAddress address, int[] partitions, boolean available) {
		this(address.getHostName(), address.getPort(), partitions, available);
	}

	public Node(String hostName, int port, boolean available) {
		this(hostName, port, new int[0], available);
	}

	public Node(String hostName, int port, int[] partitions, boolean available) {
		this.hostName = hostName;
		this.port = port;
		this.partitions = partitions;
		this.available = available;
	}

	public String getId() {
		return getHostName() + ":" + getPort();
	}

	public String getUrl() {
		return url;
	}

	public int[] getPartitions() {
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

	public String getApplicationName() {
		return applicationName;
	}

	public void setApplicationName(String applicationName) {
		this.applicationName = applicationName;
	}

	public String getServiceName() {
		return serviceName;
	}

	public void setServiceName(String serviceName) {
		this.serviceName = serviceName;
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
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}
}
