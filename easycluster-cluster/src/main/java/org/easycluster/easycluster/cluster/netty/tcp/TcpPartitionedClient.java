package org.easycluster.easycluster.cluster.netty.tcp;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.client.PartitionedNetworkClient;
import org.easycluster.easycluster.cluster.client.loadbalancer.PartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.netty.ChannelPoolFactory;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.cluster.netty.NettyIoClient;
import org.easycluster.easycluster.cluster.netty.serialization.DefaultSerializationFactory;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationConfig;
import org.easycluster.easycluster.cluster.netty.serialization.SerializationFactory;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.ChannelPipeline;
import org.jboss.netty.channel.ChannelPipelineFactory;
import org.jboss.netty.channel.DefaultChannelPipeline;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.channel.socket.nio.NioClientSocketChannelFactory;
import org.jboss.netty.handler.logging.LoggingHandler;

public class TcpPartitionedClient<PartitionedId> extends PartitionedNetworkClient<PartitionedId> {

	public TcpPartitionedClient(final NetworkClientConfig config, final PartitionedLoadBalancerFactory<PartitionedId> loadBalancerFactory) {
		super(config, loadBalancerFactory);

		ExecutorService workExecutor = Executors.newCachedThreadPool(new NamedPoolThreadFactory(String.format("client-pool-%s", config.getService())));
		ClientBootstrap bootstrap = new ClientBootstrap(new NioClientSocketChannelFactory(workExecutor, workExecutor));

		MessageContextHolder holder = new MessageContextHolder(messageRegistry, config.getStaleRequestTimeoutMins(),
				config.getStaleRequestCleanupFrequencyMins());
		final SimpleChannelHandler handler = new ClientChannelHandler(config.getService(), holder);

		bootstrap.setOption("connectTimeoutMillis", config.getConnectTimeoutMillis());
		bootstrap.setOption("tcpNoDelay", true);
		bootstrap.setOption("reuseAddress", true);
		bootstrap.setOption("keepAlive", true);

		final SerializationConfig serializationConfig = config.getSerializationConfig();
		final SerializationFactory serializationFactory = new DefaultSerializationFactory(serializationConfig);

		bootstrap.setPipelineFactory(new ChannelPipelineFactory() {

			private LoggingHandler	loggingHandler	= new LoggingHandler();

			@Override
			public ChannelPipeline getPipeline() throws Exception {
				ChannelPipeline p = new DefaultChannelPipeline();

				p.addFirst("logging", loggingHandler);
				TcpBeanEncoder encoder = new TcpBeanEncoder();
				encoder.setDebugEnabled(serializationConfig.isEncodeBytesDebugEnabled());
				encoder.setDumpBytes(serializationConfig.getDumpBytes());
				encoder.setSerialization(serializationFactory.getSerialization());
				p.addLast("encoder", encoder);

				TcpBeanDecoder decoder = new TcpBeanDecoder();
				decoder.setDebugEnabled(serializationConfig.isEncodeBytesDebugEnabled());
				decoder.setDumpBytes(serializationConfig.getDumpBytes());
				decoder.setTypeMetaInfo(serializationConfig.getTypeMetaInfo());
				decoder.setSerialization(serializationFactory.getSerialization());
				p.addLast("decoder", decoder);
				p.addLast("handler", handler);

				return p;
			}

		});

		clusterIoClient = new NettyIoClient(new ChannelPoolFactory(bootstrap, config.getMaxConnectionsPerNode(), config.getWriteTimeoutMillis()));

	}

}
