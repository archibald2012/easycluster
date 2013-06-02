package org.easycluster.easycluster.cluster;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public class NetworkClientConfig {

	private ChannelUpstreamHandler		decoder								= null;

	private ChannelDownstreamHandler	encoder								= null;

	private String						applicationName						= null;

	private String						serviceName							= null;

	private String						zooKeeperConnectString				= null;

	/**
	 * The ZooKeeper session timeout in milliseconds.
	 */
	private int							zooKeeperSessionTimeoutMillis		= ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS;

	/**
	 * The number of milliseconds to wait when opening a socket.
	 */
	private int							connectTimeoutMillis				= NetworkDefaults.CONNECT_TIMEOUT_MILLIS;

	/**
	 * The write timeout in milliseconds.
	 */
	private int							writeTimeoutMillis					= NetworkDefaults.WRITE_TIMEOUT_MILLIS;

	/**
	 * The maximum number of connections to be opened per node.
	 */
	private int							maxConnectionsPerNode				= NetworkDefaults.MAX_CONNECTIONS_PER_NODE;

	/**
	 * The time to wait before considering a request to be stale in minutes.
	 */
	private int							staleRequestTimeoutMins				= NetworkDefaults.STALE_REQUEST_TIMEOUT_MINS;

	/**
	 * The frequency to clean up stale requests in minutes.
	 */
	private int							staleRequestCleanupFrequencyMins	= NetworkDefaults.STALE_REQUEST_CLEANUP_FREQUENCY_MINS;

	private int							maxContentLength					= NetworkDefaults.REQUEST_MAX_CONTENT_LENGTH;

	public ChannelUpstreamHandler getDecoder() {
		return decoder;
	}

	public void setDecoder(ChannelUpstreamHandler decoder) {
		this.decoder = decoder;
	}

	public ChannelDownstreamHandler getEncoder() {
		return encoder;
	}

	public void setEncoder(ChannelDownstreamHandler encoder) {
		this.encoder = encoder;
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

	public String getZooKeeperConnectString() {
		return zooKeeperConnectString;
	}

	public void setZooKeeperConnectString(String zooKeeperConnectString) {
		this.zooKeeperConnectString = zooKeeperConnectString;
	}

	public int getZooKeeperSessionTimeoutMillis() {
		return zooKeeperSessionTimeoutMillis;
	}

	public void setZooKeeperSessionTimeoutMillis(int zooKeeperSessionTimeoutMillis) {
		this.zooKeeperSessionTimeoutMillis = zooKeeperSessionTimeoutMillis;
	}

	public int getConnectTimeoutMillis() {
		return connectTimeoutMillis;
	}

	public void setConnectTimeoutMillis(int connectTimeoutMillis) {
		this.connectTimeoutMillis = connectTimeoutMillis;
	}

	public int getWriteTimeoutMillis() {
		return writeTimeoutMillis;
	}

	public void setWriteTimeoutMillis(int writeTimeoutMillis) {
		this.writeTimeoutMillis = writeTimeoutMillis;
	}

	public int getMaxConnectionsPerNode() {
		return maxConnectionsPerNode;
	}

	public void setMaxConnectionsPerNode(int maxConnectionsPerNode) {
		this.maxConnectionsPerNode = maxConnectionsPerNode;
	}

	public int getStaleRequestTimeoutMins() {
		return staleRequestTimeoutMins;
	}

	public void setStaleRequestTimeoutMins(int staleRequestTimeoutMins) {
		this.staleRequestTimeoutMins = staleRequestTimeoutMins;
	}

	public int getStaleRequestCleanupFrequencyMins() {
		return staleRequestCleanupFrequencyMins;
	}

	public void setStaleRequestCleanupFrequencyMins(int staleRequestCleanupFrequencyMins) {
		this.staleRequestCleanupFrequencyMins = staleRequestCleanupFrequencyMins;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

}
