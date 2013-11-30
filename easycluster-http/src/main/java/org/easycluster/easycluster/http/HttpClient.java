package org.easycluster.easycluster.http;

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
import org.jboss.netty.handler.codec.http.HttpClientCodec;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;

public class HttpClient extends NetworkClient {

	public HttpClient(final NetworkClientConfig config, final LoadBalancerFactory loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService workExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getService())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(workExecutor, workExecutor));

		final HttpRequestEncoder encoder = new HttpRequestEncoder();
		encoder.setSerialization(new DefaultSerializationFactory(config.getEncodeSerializeConfig()).getSerialization());
		encoder.setDebugEnabled(config.getEncodeSerializeConfig().isSerializeBytesDebugEnabled());
		encoder.setDumpBytes(config.getEncodeSerializeConfig().getDumpBytes());

		final HttpResponseDecoder decoder = new HttpResponseDecoder();
		decoder.setSerialization(new DefaultSerializationFactory(config.getDecodeSerializeConfig()).getSerialization());
		decoder.setDebugEnabled(config.getDecodeSerializeConfig().isSerializeBytesDebugEnabled());
		decoder.setDumpBytes(config.getDecodeSerializeConfig().getDumpBytes());
		decoder.setTypeMetaInfo(config.getDecodeSerializeConfig().getTypeMetaInfo());

		MessageContextHolder holder = new MessageContextHolder(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());
		final HttpClientChannelHandler handler = new HttpClientChannelHandler(holder);
		handler.setRequestTransformer(encoder);
		handler.setResponseTransformer(decoder);

		final SSLContext sslContext = config.getSslConfig() != null ? new SSLContextFactory().createDummySslContext(config.getSslConfig().getProtocol()) : null;

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

				p.addLast("codec", new HttpClientCodec());
				p.addLast("aggregator", new HttpChunkAggregator(config.getDecodeSerializeConfig().getMaxContentLength()));
				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
