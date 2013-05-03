package org.easycluster.easycluster.cluster.netty;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.codec.NettyBeanDecoder;
import org.easycluster.easycluster.cluster.netty.codec.NettyBeanEncoder;
import org.easycluster.easycluster.cluster.netty.endpoint.IEndpointListener;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easycluster.easycluster.cluster.server.ThreadPoolMessageExecutor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class NettyNetworkServer extends NetworkServer {

	private OneToOneDecoder decoder = new NettyBeanDecoder();
	private OneToOneEncoder encoder = new NettyBeanEncoder();

	private int requestThreadCorePoolSize = NetworkDefaults.REQUEST_THREAD_CORE_POOL_SIZE;
	private int requestThreadMaxPoolSize = NetworkDefaults.REQUEST_THREAD_MAX_POOL_SIZE;
	private int requestThreadKeepAliveTimeSecs = NetworkDefaults.REQUEST_THREAD_KEEP_ALIVE_TIME_SECS;
	private int idleTime = NetworkDefaults.ALLIDLE_TIMEOUT_MILLIS;

	private IEndpointListener endpointListener;

	public NettyNetworkServer(String applicationName, String serviceName,
			String zooKeeperConnectString) {
		super(applicationName, serviceName, zooKeeperConnectString);
	}

	public void start() {

		messageExecutor = new ThreadPoolMessageExecutor(messageClosureRegistry,
				getRequestThreadCorePoolSize(), getRequestThreadMaxPoolSize(),
				getRequestThreadKeepAliveTimeSecs());

		ExecutorService workerExecutor = Executors
				.newCachedThreadPool(new NamedPoolThreadFactory(String.format(
						"netty-server-pool-%s", serviceName)));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format(
				"netty-server-group-%s", serviceName));

		final ServerChannelHandler requestHandler = new ServerChannelHandler(
				channelGroup, messageClosureRegistry, messageExecutor);
		requestHandler.setEndpointListener(endpointListener);

		ServerBootstrap bootstrap = new ServerBootstrap(
				new NioServerSocketChannelFactory(workerExecutor,
						workerExecutor));

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler loggingHandler = new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = Channels.pipeline();

				p.addFirst("logging", loggingHandler);

				p.addLast("encoder", encoder);
				p.addLast("decoder", decoder);

				p.addLast("idleHandler", new IdleStateHandler(
						new HashedWheelTimer(), 0, 0, getIdleTime(),
						TimeUnit.SECONDS));

				p.addLast("requestHandler", requestHandler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);

		bind(port, partitionIds, markAvailableWhenConnected);
	}

	public void setDecoder(OneToOneDecoder decoder) {
		this.decoder = decoder;
	}

	public void setEncoder(OneToOneEncoder encoder) {
		this.encoder = encoder;
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

	public void setRequestThreadKeepAliveTimeSecs(
			int requestThreadKeepAliveTimeSecs) {
		this.requestThreadKeepAliveTimeSecs = requestThreadKeepAliveTimeSecs;
	}

	public int getIdleTime() {
		return idleTime;
	}

	public void setIdleTime(int idleTime) {
		this.idleTime = idleTime;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public void setPartitionIds(int[] partitionIds) {
		this.partitionIds = partitionIds;
	}

	public void setMarkAvailableWhenConnected(boolean markAvailableWhenConnected) {
		this.markAvailableWhenConnected = markAvailableWhenConnected;
	}

	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

}
