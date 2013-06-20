package org.easycluster.easycluster.cluster.netty.websocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoServer;
import org.easycluster.easycluster.cluster.netty.tcp.ServerChannelHandler;
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
import org.jboss.netty.handler.codec.http.HttpRequestDecoder;
import org.jboss.netty.handler.codec.http.HttpResponseEncoder;
import org.jboss.netty.handler.logging.LoggingHandler;
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
 * @version $Id: WebSocketAcceptor.java 59 2012-02-24 08:40:46Z archie $
 */
public class WebsocketServer extends NetworkServer {

	public WebsocketServer(final NetworkServerConfig config) {
		super(config);

		MessageExecutor messageExecutor = new PartitionedThreadPoolMessageExecutor(messageClosureRegistry, 1, 1, config.getRequestThreadKeepAliveTimeSecs(),
				config.getRequestThreadCorePoolSize());

		ExecutorService workerExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("websocket-server-pool-%s",
				config.getServiceName())));
		ChannelGroup channelGroup = new DefaultChannelGroup(String.format("websocket-server-group-%s", config.getServiceName()));

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
				p.addLast("httpRequestDecoder", new HttpRequestDecoder());
				p.addLast("httpResponseEncoder", new HttpResponseEncoder());
				p.addLast("aggregator", new HttpChunkAggregator(config.getSerializationConfig().getMaxContentLength()));
				p.addLast("idleHandler", new IdleStateHandler(new HashedWheelTimer(), 0, 0, config.getIdleTime(), TimeUnit.SECONDS));
				p.addLast("ws-handler", new WebSocketServerHandshakerHandler());
				p.addLast("handler", requestHandler);

				return p;
			}
		});

		clusterIoServer = new NettyIoServer(bootstrap, channelGroup);
	}

}
