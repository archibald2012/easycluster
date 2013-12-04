package org.easycluster.easycluster.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoServer;
import org.easycluster.easycluster.cluster.security.BlackList;
import org.easycluster.easycluster.cluster.serialization.DefaultSerializationFactory;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
import org.easycluster.easycluster.cluster.server.NetworkServer;
import org.easycluster.easycluster.cluster.server.PartitionedThreadPoolMessageExecutor;
import org.easycluster.easycluster.cluster.server.ThreadPoolMessageExecutor;
import org.easycluster.easycluster.cluster.ssl.SSLContextFactory;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;
import org.jboss.netty.channel.socket.nio.NioServerSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpContentCompressor;
import org.jboss.netty.handler.codec.http.HttpServerCodec;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

public class HttpServer extends NetworkServer {

	public HttpServer(final NetworkServerConfig config) {
		super(config);

		MessageExecutor messageExecutor = null;
		if (config.isRequestThreadPartitioned()) {
			messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, config.getRequestThreadKeepAliveTimeSecs(),
					config.getRequestThreadCorePoolSize());
		} else {
			messageExecutor = new ThreadPoolMessageExecutor("threadpool-message-executor", config.getRequestThreadCorePoolSize(),
					config.getRequestThreadMaxPoolSize(), config.getRequestThreadKeepAliveTimeSecs(), messageClosureRegistry);
		}

		ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("http-server-pool-%s", config.getService())));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("http-server-group-%s", config.getService()));

		BlackList blackList = (config.getBlacklist() != null) ? new BlackList(config.getBlacklist(), config.getClusterEventHandler()) : null;
		final HttpServerChannelHandler requestHandler = new HttpServerChannelHandler(channelGroup, messageClosureRegistry, messageExecutor, blackList);
		requestHandler.setEndpointListener(config.getEndpointListener());

		HttpResponseEncoder encoder = new HttpResponseEncoder();
		encoder.setSerialization(new DefaultSerializationFactory(config.getEncodeSerializeConfig()).getSerialization());
		encoder.setDebugEnabled(config.getEncodeSerializeConfig().isSerializeBytesDebugEnabled());
		encoder.setDumpBytes(config.getEncodeSerializeConfig().getDumpBytes());

		requestHandler.setResponseTransformer(encoder);

		HttpRequestDecoder decoder = new HttpRequestDecoder();
		decoder.setSerialization(new DefaultSerializationFactory(config.getDecodeSerializeConfig()).getSerialization());
		decoder.setTypeMetaInfo(config.getDecodeSerializeConfig().getTypeMetaInfo());
		decoder.setDumpBytes(config.getDecodeSerializeConfig().getDumpBytes());
		decoder.setDebugEnabled(config.getDecodeSerializeConfig().isSerializeBytesDebugEnabled());

		requestHandler.setRequestTransformer(decoder);

		final SSLContext sslContext = config.getSslConfig() != null ? new SSLContextFactory().createSslContext(config.getSslConfig()) : null;

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

				if (sslContext != null) {
					SSLEngine engine = sslContext.createSSLEngine();
					engine.setUseClientMode(false);
					p.addLast("ssl", new SslHandler(engine));
				}

				// HttpServerCodec is not thread-safe
				p.addLast("codec", new HttpServerCodec());
				p.addLast("aggregator", new HttpChunkAggregator(config.getDecodeSerializeConfig().getMaxContentLength()));
				p.addLast("idleHandler", idleStateHandler);
				// Remove the following line if you don't want automatic content
				// compression.
				p.addLast("deflater", new HttpContentCompressor());
				p.addLast("handler", requestHandler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);
	}
}
