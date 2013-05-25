package org.easycluster.easycluster.cluster.netty;

import java.net.InetSocketAddress;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.ClusterIoClient;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.exception.ChannelPoolClosedException;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyIoClient implements ClusterIoClient {

	private static final Logger						LOGGER				= LoggerFactory.getLogger(NettyIoClient.class);

	private ChannelPoolFactory						channelPoolFactory	= null;
	private ConcurrentHashMap<Node, ChannelPool>	channelPools		= new ConcurrentHashMap<Node, ChannelPool>();

	public NettyIoClient(ChannelPoolFactory channelPoolFactory) {
		this.channelPoolFactory = channelPoolFactory;
	}

	@Override
	public void sendMessage(Node node, Object message, Closure closure) {
		if (node == null) {
			throw new IllegalArgumentException("node is null!");
		}
		if (message == null) {
			throw new IllegalArgumentException("message is null!");
		}
		if (closure == null) {
			throw new IllegalArgumentException("responseCallback is null!");
		}

		ChannelPool pool = channelPools.get(node);
		if (pool == null) {

			pool = channelPoolFactory.newChannelPool(new InetSocketAddress(node.getHostName(), node.getPort()));
			channelPools.putIfAbsent(node, pool);
			pool = channelPools.get(node);
		}

		try {
			pool.sendRequest(new MessageContext(message, closure));
		} catch (ChannelPoolClosedException ex) {
			LOGGER.error("Failed to send message.", ex);
			// ChannelPool was closed, try again
			sendMessage(node, message, closure);
		}
	}

	@Override
	public void nodesChanged(Set<Node> nodes) {
		for (Node node : channelPools.keySet()) {
			if (!nodes.contains(node)) {
				ChannelPool pool = channelPools.remove(node);
				pool.close();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Closing pool for unavailable node: {}", node);
				}
			}
		}
	}

	@Override
	public void shutdown() {
		for (Node node : channelPools.keySet()) {
			ChannelPool pool = channelPools.get(node);
			if (pool != null) {
				pool.close();
				channelPools.remove(node);
			}
		}
		channelPoolFactory.shutdown();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("NettyClusterIoClient shut down");
		}
	}

}
