package org.easycluster.easycluster.cluster.netty.websocket;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.client.NetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.ChannelPoolFactory;
import org.easycluster.easycluster.cluster.netty.NettyIoClient;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpRequestEncoder;
import org.jboss.netty.handler.codec.http.HttpResponseDecoder;
import org.jboss.netty.handler.logging.LoggingHandler;

public class WebSocketNetworkClient extends NetworkClient {

	public WebSocketNetworkClient(final NetworkClientConfig config, final LoadBalancerFactory loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService executor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getServiceName())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(executor, executor));

		final SimpleChannelHandler handler = new WebSocketClientHandler(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());

		bootstrap.setOption("connectTimeoutMillis", config.getConnectTimeoutMillis());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				p.addFirst("logging", loggingHandler);
				p.addLast("aggregator", new HttpChunkAggregator(config.getMaxContentLength()));
				p.addLast("httpResponseDecoder", new HttpResponseDecoder());
				p.addLast("httpRequestEncoder", new HttpRequestEncoder());
				p.addLast("decoder", config.getDecoder());
				p.addLast("encoder", config.getEncoder());
				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
