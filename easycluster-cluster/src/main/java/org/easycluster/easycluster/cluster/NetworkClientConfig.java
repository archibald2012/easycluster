package org.easycluster.easycluster.cluster;

import org.easycluster.easycluster.cluster.serialization.SerializationConfig;

public class NetworkClientConfig {

	/**
	 * The group of service.
	 */
	private String				serviceGroup						= null;

	/**
	 * The service name.
	 */
	private String				service								= null;

	/**
	 * The ZooKeeper connection string.
	 */
	private String				zooKeeperConnectString				= null;

	/**
	 * The ZooKeeper session timeout in milliseconds.
	 */
	private int					zooKeeperSessionTimeoutMillis		= ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS;

	private boolean				mutexInstance						= false;

	/**
	 * The number of milliseconds to wait when opening a socket.
	 */
	private int					connectTimeoutMillis				= NetworkDefaults.CONNECT_TIMEOUT_MILLIS;

	/**
	 * The write timeout in milliseconds.
	 */
	private int					writeTimeoutMillis					= NetworkDefaults.WRITE_TIMEOUT_MILLIS;

	/**
	 * The maximum number of connections to be opened per node.
	 */
	private int					maxConnectionsPerNode				= NetworkDefaults.MAX_CONNECTIONS_PER_NODE;

	/**
	 * The time to wait before considering a request to be stale in minutes.
	 */
	private int					staleRequestTimeoutMins				= NetworkDefaults.STALE_REQUEST_TIMEOUT_MINS;

	/**
	 * The frequency to clean up stale requests in minutes.
	 */
	private int					staleRequestCleanupFrequencyMins	= NetworkDefaults.STALE_REQUEST_CLEANUP_FREQUENCY_MINS;

	/**
	 * The serialization configuration.
	 */
	private SerializationConfig	decodeSerializeConfig				= null;

	private SerializationConfig	encodeSerializeConfig				= null;

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

	public SerializationConfig getDecodeSerializeConfig() {
		return decodeSerializeConfig;
	}

	public void setDecodeSerializeConfig(SerializationConfig decodeSerializeConfig) {
		this.decodeSerializeConfig = decodeSerializeConfig;
	}

	public SerializationConfig getEncodeSerializeConfig() {
		return encodeSerializeConfig;
	}

	public void setEncodeSerializeConfig(SerializationConfig encodeSerializeConfig) {
		this.encodeSerializeConfig = encodeSerializeConfig;
	}

	public boolean isMutexInstance() {
		return mutexInstance;
	}

	public void setMutexInstance(boolean mutexInstance) {
		this.mutexInstance = mutexInstance;
	}

}
