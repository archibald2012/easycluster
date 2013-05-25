package org.easycluster.easycluster.cluster.netty.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoServer;
import org.easycluster.easycluster.cluster.netty.ServerChannelHandler;
import org.easycluster.easycluster.cluster.netty.endpoint.IEndpointListener;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easycluster.easycluster.cluster.server.PartitionedThreadPoolMessageExecutor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class HttpNetworkServer extends NetworkServer {
	private OneToOneDecoder		decoder							= new HttpRequestDecoder();
	private OneToOneEncoder		encoder							= new HttpResponseEncoder();

	private int					requestThreadCorePoolSize		= NetworkDefaults.REQUEST_THREAD_CORE_POOL_SIZE;
	private int					requestThreadMaxPoolSize		= NetworkDefaults.REQUEST_THREAD_MAX_POOL_SIZE;
	private int					requestThreadKeepAliveTimeSecs	= NetworkDefaults.REQUEST_THREAD_KEEP_ALIVE_TIME_SECS;
	private int					idleTime						= NetworkDefaults.ALLIDLE_TIMEOUT_MILLIS;

	// 100M
	private int					maxContentLength				= 100 * 1024 * 1024;

	private IEndpointListener	endpointListener;

	public HttpNetworkServer(String applicationName, String serviceName, String zooKeeperConnectString) {
		super(applicationName, serviceName, zooKeeperConnectString);
	}

	public void start() {

		messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, getRequestThreadKeepAliveTimeSecs(),
				getRequestThreadCorePoolSize());

		ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("netty-server-pool-%s", serviceName)));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("netty-server-group-%s", serviceName));

		final ServerChannelHandler requestHandler = new ServerChannelHandler(channelGroup, messageClosureRegistry, messageExecutor);
		requestHandler.setEndpointListener(endpointListener);

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(workerExecutor, workerExecutor));

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = Channels.pipeline();

				p.addFirst("logging", loggingHandler);
				// HttpServerCodec是非线程安全的,不能所有Channel使用同一个
				p.addLast("codec", new HttpServerCodec());
				p.addLast("aggregator", new HttpChunkAggregator(maxContentLength));

				p.addLast("httpResponseEncoder", encoder);
				p.addLast("httpRequestDecoder", decoder);

				p.addLast("idleHandler", new IdleStateHandler(new HashedWheelTimer(), 0, 0, getIdleTime(), TimeUnit.SECONDS));

				p.addLast("handler", requestHandler);

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

	public void setRequestThreadKeepAliveTimeSecs(int requestThreadKeepAliveTimeSecs) {
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

	public void setMarkAvailableWhenConnected(boolean markAvailableWhenConnected) {
		this.markAvailableWhenConnected = markAvailableWhenConnected;
	}

	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

	public void setMaxContentLength(int maxContentLength) {
		this.maxContentLength = maxContentLength;
	}

}
