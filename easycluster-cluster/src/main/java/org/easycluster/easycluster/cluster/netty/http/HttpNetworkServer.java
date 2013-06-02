package org.easycluster.easycluster.cluster.netty.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoServer;
import org.easycluster.easycluster.cluster.netty.ServerChannelHandler;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
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
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class HttpNetworkServer extends NetworkServer {

	public HttpNetworkServer(final NetworkServerConfig config) {
		super(config);

		MessageExecutor messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, config.getRequestThreadKeepAliveTimeSecs(),
				config.getRequestThreadCorePoolSize());

		ExecutorService workerExecutor = Executors
				.newCachedThreadPool(new NamedPoolThreadFactory(String.format("http-server-pool-%s", config.getServiceName())));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("http-server-group-%s", config.getServiceName()));

		final ServerChannelHandler requestHandler = new ServerChannelHandler(channelGroup, messageClosureRegistry, messageExecutor);
		requestHandler.setEndpointListener(config.getEndpointListener());

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(workerExecutor, workerExecutor));

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = Channels.pipeline();

				p.addFirst("logging", loggingHandler);
				// HttpServerCodec是非线程安全的,不能所有Channel使用同一个
				p.addLast("codec", new HttpServerCodec());
				p.addLast("aggregator", new HttpChunkAggregator(config.getMaxContentLength()));
				p.addLast("encoder", config.getEncoder());
				p.addLast("decoder", config.getDecoder());
				p.addLast("idleHandler", new IdleStateHandler(new HashedWheelTimer(), 0, 0, config.getIdleTime(), TimeUnit.SECONDS));
				p.addLast("handler", requestHandler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);
	}

}
