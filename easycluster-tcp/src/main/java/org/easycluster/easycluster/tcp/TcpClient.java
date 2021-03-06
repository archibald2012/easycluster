package org.easycluster.easycluster.tcp;

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
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.ssl.SSLContextFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;
import org.jboss.netty.handler.ssl.SslHandler;

public class TcpClient extends NetworkClient {

	public TcpClient(final NetworkClientConfig config, final LoadBalancerFactory loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService workExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getService())));

		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(workExecutor, workExecutor));

		bootstrap.setOption("connectTimeoutMillis", config.getConnectTimeoutMillis());
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("keepAlive", true);

		final SSLContext sslContext = config.getSslConfig() != null ? new SSLContextFactory().createSslContext(config.getSslConfig()) : null;

		MessageContextHolder holder = new MessageContextHolder(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());
		final SimpleChannelHandler handler = new ClientChannelHandler(holder);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				if (sslContext != null) {
					SSLEngine engine = sslContext.createSSLEngine();
					engine.setUseClientMode(true);
					engine.setNeedClientAuth(true);
					p.addLast("ssl", new SslHandler(engine));
				}

				p.addFirst("logging", loggingHandler);

				TcpBeanEncoder encoder = new TcpBeanEncoder();
				final SerializationConfig encodeSerializeConfig = config.getEncodeSerializeConfig();
				encoder.setDebugEnabled(encodeSerializeConfig.isSerializeBytesDebugEnabled());
				encoder.setDumpBytes(encodeSerializeConfig.getDumpBytes());
				encoder.setSerialization(new DefaultSerializationFactory(encodeSerializeConfig).getSerialization());
				p.addLast("encoder", encoder);

				TcpBeanDecoder decoder = new TcpBeanDecoder();
				SerializationConfig decodeSerializeConfig = config.getDecodeSerializeConfig();
				decoder.setDebugEnabled(decodeSerializeConfig.isSerializeBytesDebugEnabled());
				decoder.setDumpBytes(decodeSerializeConfig.getDumpBytes());
				decoder.setTypeMetaInfo(decodeSerializeConfig.getTypeMetaInfo());
				decoder.setMaxMessageLength(decodeSerializeConfig.getMaxContentLength());
				decoder.setSerialization(new DefaultSerializationFactory(decodeSerializeConfig).getSerialization());
				p.addLast("decoder", decoder);

				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
