package org.easycluster.easycluster.cluster.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.cluster.client.NetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.ChannelUpstreamHandler;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;

public class NettyNetworkClient extends NetworkClient {

	private ChannelUpstreamHandler		decoder								= null;
	private ChannelDownstreamHandler	encoder								= null;

	private int							connectTimeoutMillis				= NetworkDefaults.CONNECT_TIMEOUT_MILLIS;
	private int							writeTimeoutMillis					= NetworkDefaults.WRITE_TIMEOUT_MILLIS;
	private int							maxConnectionsPerNode				= NetworkDefaults.MAX_CONNECTIONS_PER_NODE;
	private int							staleRequestTimeoutMins				= NetworkDefaults.STALE_REQUEST_TIMEOUT_MINS;
	private int							staleRequestCleanupFrequenceMins	= NetworkDefaults.STALE_REQUEST_CLEANUP_FREQUENCY_MINS;

	public NettyNetworkClient(String applicationName, String serviceName, String zooKeeperConnectString, LoadBalancerFactory loadBalancerFactory) {
		super(applicationName, serviceName, zooKeeperConnectString, loadBalancerFactory);
	}

	public void start() {

		ExecutorService executor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", getServiceName())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor));

		final SimpleChannelHandler handler = new ClientChannelHandler(messageRegistry, getStaleRequestTimeoutMins(), getStaleRequestCleanupFrequenceMins());

		bootstrap.setOption("connectTimeoutMillis", getConnectTimeoutMillis());
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				p.addFirst("logging", loggingHandler);
				p.addLast("decoder", decoder);
				p.addLast("encoder", encoder);
				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, getMaxConnectionsPerNode(), getWriteTimeoutMillis()));

		super.start();
	}

	public void setDecoder(ChannelUpstreamHandler decoder) {
		this.decoder = decoder;
	}

	public void setEncoder(ChannelDownstreamHandler encoder) {
		this.encoder = encoder;
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

	public int getStaleRequestCleanupFrequenceMins() {
		return staleRequestCleanupFrequenceMins;
	}

	public void setStaleRequestCleanupFrequenceMins(int staleRequestCleanupFrequenceMins) {
		this.staleRequestCleanupFrequenceMins = staleRequestCleanupFrequenceMins;
	}
}
