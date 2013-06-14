package org.easycluster.easycluster.cluster.netty.http;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.client.NetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.ChannelPoolFactory;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.cluster.netty.NettyIoClient;
import org.easycluster.easycluster.cluster.netty.codec.DefaultSerializationFactory;
import org.easycluster.easycluster.cluster.netty.codec.SerializationFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.codec.http.HttpChunkAggregator;
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.logging.LoggingHandler;

public class HttpConnector extends NetworkClient {

	public HttpConnector(final NetworkClientConfig config, final LoadBalancerFactory loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService workExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getServiceName())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(workExecutor, workExecutor));

		SerializationFactory codecFactory = new DefaultSerializationFactory(config.getSerializationConfig());
		final HttpRequestEncoder encoder = new HttpRequestEncoder();
		final HttpResponseDecoder decoder = new HttpResponseDecoder();
		encoder.setBytesEncoder(codecFactory.getEncoder());
		decoder.setBytesDecoder(codecFactory.getDecoder());

		MessageContextHolder holder = new MessageContextHolder(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());
		final HttpClientChannelHandler handler = new HttpClientChannelHandler(holder);
		handler.setRequestTransformer(encoder);
		handler.setResponseTransformer(decoder);

		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				p.addFirst("logging", loggingHandler);
				p.addLast("codec", new HttpClientCodec());
				p.addLast("aggregator", new HttpChunkAggregator(config.getSerializationConfig().getMaxContentLength()));
				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
