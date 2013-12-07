package org.easycluster.easycluster.websocket;

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
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateHandler;
import org.jboss.netty.util.HashedWheelTimer;

/**
 * A HTTP server which serves Web Socket requests.
 * <ul>
 * <li>Hixie-76/HyBi-00 Safari 5+, Chrome 4-13 and Firefox 4 supports this
 * standard.
 * <li>HyBi-10 Chrome 14-15, Firefox 7 and IE 10 Developer Preview supports this
 * standard.
 * </ul>
 * This server illustrates support for the different web socket specification
 * versions and will work with:
 * 
 * <ul>
 * <li>Safari 5+ (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 6-13 (draft-ietf-hybi-thewebsocketprotocol-00)
 * <li>Chrome 14+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * <li>Chrome 16+ (RFC 6455 aka draft-ietf-hybi-thewebsocketprotocol-17)
 * <li>Firefox 7+ (draft-ietf-hybi-thewebsocketprotocol-10)
 * </ul>
 * 
 * @author wangqi
 * @version $Id: WebSocketServer.java 59 2012-02-24 08:40:46Z archie $
 */
public class WebSocketServer extends NetworkServer {

	public WebSocketServer(final NetworkServerConfig config) {
		super(config);

		MessageExecutor messageExecutor = null;
		if (config.isRequestThreadPartitioned()) {
			messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, config.getRequestThreadKeepAliveTimeSecs(),
					config.getRequestThreadCorePoolSize());
		} else {
			messageExecutor = new ThreadPoolMessageExecutor("threadpool-message-executor", config.getRequestThreadCorePoolSize(),
					config.getRequestThreadMaxPoolSize(), config.getRequestThreadKeepAliveTimeSecs(), messageClosureRegistry);
		}

		ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(
				String.format("websocket-server-pool-%s", config.getService())));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("websocket-server-group-%s", config.getService()));

		final TextWebSocketFrameDecoder decoder = new TextWebSocketFrameDecoder();
		decoder.setSerialization(new DefaultSerializationFactory(config.getDecodeSerializeConfig()).getSerialization());
		decoder.setTypeMetaInfo(config.getDecodeSerializeConfig().getTypeMetaInfo());

		BlackList blackList = (config.getBlacklist() != null) ? new BlackList(config.getBlacklist(), config.getClusterEventHandler()) : null;
		final WebSocketServerChannelHandler handler = new WebSocketServerChannelHandler(channelGroup, messageClosureRegistry, messageExecutor, blackList);
		handler.setEndpointListener(config.getEndpointListener());
		handler.setWebSocketFrameDecoder(decoder);

		final SSLContext sslContext = config.getSslConfig() != null ? new SSLContextFactory().createSslContext(config.getSslConfig()) : null;

		ServerBootstrap bootstrap = new ServerBootstrap(new NioServerSocketChannelFactory(workerExecutor, workerExecutor));

		bootstrap.setOption("backlog", 20000);
		bootstrap.setOption("tcpNoDelay", true);

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

				p.addLast("decoder", new HttpRequestDecoder());
				p.addLast("aggregator", new HttpChunkAggregator(config.getDecodeSerializeConfig().getMaxContentLength()));
				p.addLast("encoder", new HttpResponseEncoder());
				p.addLast("idleHandler", idleStateHandler);

				p.addLast("handler", handler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);
	}

}
