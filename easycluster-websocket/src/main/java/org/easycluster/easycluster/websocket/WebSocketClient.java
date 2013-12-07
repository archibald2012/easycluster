package org.easycluster.easycluster.websocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLEngine;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.client.NetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.ChannelPoolFactory;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.cluster.netty.NettyIoClient;
import org.easycluster.easycluster.cluster.serialization.DefaultSerializationFactory;
import org.easycluster.easycluster.cluster.ssl.SSLContextFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;

public class WebSocketClient extends NetworkClient {

	public WebSocketClient(final NetworkClientConfig config, final LoadBalancerFactory loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService workExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getService())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(workExecutor, workExecutor));

		final TextWebSocketFrameDecoder decoder = new TextWebSocketFrameDecoder();
		decoder.setSerialization(new DefaultSerializationFactory(config.getDecodeSerializeConfig()).getSerialization());
		decoder.setTypeMetaInfo(config.getDecodeSerializeConfig().getTypeMetaInfo());

		MessageContextHolder holder = new MessageContextHolder(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());
		final WebSocketClientChannelHandler handler = new WebSocketClientChannelHandler(holder);
		handler.setWebSocketFrameDecoder(decoder);

		final SSLContext sslContext = config.getSslConfig() != null ? new SSLContextFactory().createSslContext(config.getSslConfig()) : null;

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				p.addFirst("logging", loggingHandler);

				if (sslContext != null) {
					SSLEngine engine = sslContext.createSSLEngine();
					engine.setUseClientMode(true);
					engine.setNeedClientAuth(true);
					p.addLast("ssl", new SslHandler(engine));
				}

				p.addLast("aggregator", new HttpChunkAggregator(config.getDecodeSerializeConfig().getMaxContentLength()));
				p.addLast("decoder", new HttpResponseDecoder());
				p.addLast("encoder", new HttpRequestEncoder());
				p.addLast("ws-handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
