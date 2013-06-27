package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoServer;
import org.easycluster.easycluster.cluster.netty.serialization.DefaultSerializationFactory;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationFactory;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easycluster.easycluster.cluster.server.PartitionedThreadPoolMessageExecutor;
import org.easycluster.easycluster.cluster.server.ThreadPoolMessageExecutor;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class TcpServer extends NetworkServer {

	public TcpServer(final NetworkServerConfig config) {
		super(config);

		MessageExecutor messageExecutor = null;
		if (config.isRequestThreadPartitioned()) {
			messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, config.getRequestThreadKeepAliveTimeSecs(),
					config.getRequestThreadCorePoolSize());
		} else {
			messageExecutor = new ThreadPoolMessageExecutor("threadpool-message-executor", config.getRequestThreadCorePoolSize(),
					config.getRequestThreadMaxPoolSize(), config.getRequestThreadKeepAliveTimeSecs(), messageClosureRegistry);
		}

		ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("tcp-server-pool-%s", config.getService())));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("netty-server-group-%s", config.getService()));

		final SerializationConfig serializationConfig = config.getSerializationConfig();
		final SerializationFactory serializationFactory = new DefaultSerializationFactory(serializationConfig);

		final ServerChannelHandler requestHandler = new ServerChannelHandler(config.getService(), channelGroup, messageClosureRegistry, messageExecutor);
		requestHandler.setEndpointListener(config.getEndpointListener());

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(workerExecutor, workerExecutor));

		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("child.tcpNoDelay", true);
		bootstrap.setOption("child.reuseAddress", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler		loggingHandler		= new LoggingHandler();
			private IdleStateHandler	idleStateHandler	= new IdleStateHandler(new HashedWheelTimer(), 0, 0, config.getIdleTime(), TimeUnit.SECONDS);

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = Channels.pipeline();

				p.addFirst("logging", loggingHandler);
				p.addLast("idleHandler", idleStateHandler);

				TcpBeanEncoder encoder = new TcpBeanEncoder();
				encoder.setDebugEnabled(serializationConfig.isEncodeBytesDebugEnabled());
				encoder.setDumpBytes(serializationConfig.getDumpBytes());
				encoder.setSerialization(serializationFactory.getSerialization());
				p.addLast("encoder", encoder);

				TcpBeanDecoder decoder = new TcpBeanDecoder();
				decoder.setDebugEnabled(serializationConfig.isEncodeBytesDebugEnabled());
				decoder.setDumpBytes(serializationConfig.getDumpBytes());
				decoder.setTypeMetaInfo(serializationConfig.getTypeMetaInfo());
				decoder.setMaxMessageLength(serializationConfig.getMaxContentLength());
				decoder.setSerialization(serializationFactory.getSerialization());
				p.addLast("decoder", decoder);

				p.addLast("handler", requestHandler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);
	}

}
