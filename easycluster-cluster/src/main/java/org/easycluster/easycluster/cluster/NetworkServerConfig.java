package org.easycluster.easycluster.cluster;

import org.easycluster.easycluster.cluster.common.SystemUtil;
import org.easycluster.easycluster.cluster.netty.endpoint.IEndpointListener;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public class NetworkServerConfig {

	private ChannelUpstreamHandler		decoder							= null;

	private ChannelDownstreamHandler	encoder							= null;

	private IEndpointListener			endpointListener				= null;

	private String						applicationName					= null;

	private String						serviceName						= null;

	private String						zooKeeperConnectString			= null;

	/**
	 * The ZooKeeper session timeout in milliseconds.
	 */
	private int							zooKeeperSessionTimeoutMillis	= ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS;

	/**
	 * The number of milliseconds to wait when opening a socket.
	 */
	private int							idleTime						= NetworkDefaults.ALLIDLE_TIMEOUT_MILLIS;

	/**
	 * The number of core request threads.
	 */
	private int							requestThreadCorePoolSize		= NetworkDefaults.REQUEST_THREAD_CORE_POOL_SIZE;

	/**
	 * The max number of core request threads.
	 */
	private int							requestThreadMaxPoolSize		= NetworkDefaults.REQUEST_THREAD_MAX_POOL_SIZE;

	/**
	 * The request thread timeout in seconds.
	 */
	private int							requestThreadKeepAliveTimeSecs	= NetworkDefaults.REQUEST_THREAD_KEEP_ALIVE_TIME_SECS;

	private int							maxContentLength				= NetworkDefaults.REQUEST_MAX_CONTENT_LENGTH;

	/**
	 * The serialize type, binary|json|java|hessian
	 */
	private String						serializeType					= "binary";

	private Integer[]					partitions						= new Integer[0];

	private String						version							= null;

	private String						ip								= SystemUtil.getIpAddress();

	private int							port							= -1;

	private String						url								= null;

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

	public int getRequestThreadCorePoolSize() {
		return requestThreadCorePoolSize;
	}

	public void setRequestThreadCorePoolSize(int requestThreadCorePoolSize) {
		this.requestThreadCorePoolSize = requestThreadCorePoolSize;
	}

	public int getRequestThreadMaxPoolSize() {
		return requestThreadMaxPoolSize;
	}

	public void setRequestThreadMaxPoolSize(int requestThreadMaxPoolSize) {
		this.requestThreadMaxPoolSize = requestThreadMaxPoolSize;
	}

	public int getRequestThreadKeepAliveTimeSecs() {
		return requestThreadKeepAliveTimeSecs;
	}

	public void setRequestThreadKeepAliveTimeSecs(int requestThreadKeepAliveTimeSecs) {
		this.requestThreadKeepAliveTimeSecs = requestThreadKeepAliveTimeSecs;
	}

	public IEndpointListener getEndpointListener() {
		return endpointListener;
	}

	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

	public int getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(int idleTime) {
		this.idleTime = idleTime;
	}

	public int getMaxContentLength() {
		return maxContentLength;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getSerializeType() {
		return serializeType;
	}

	public void setSerializeType(String serializeType) {
		this.serializeType = serializeType;
	}

	public Integer[] getPartitions() {
		return partitions;
	}

	public void setPartitions(Integer[] partitions) {
		this.partitions = partitions;
	}

	public String getVersion() {
		return version;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public String getIp() {
		return ip;
	}

	public void setIp(String ip) {
		this.ip = ip;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
