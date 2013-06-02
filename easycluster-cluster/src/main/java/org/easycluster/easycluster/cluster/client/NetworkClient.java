package org.easycluster.easycluster.cluster.client;

import java.util.Set;
import java.util.concurrent.Future;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancer;
import org.easycluster.easycluster.cluster.client.loadbalancer.LoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.ResponseFuture;
import org.easycluster.easycluster.cluster.exception.InvalidClusterException;
import org.easycluster.easycluster.cluster.exception.NoNodesAvailableException;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkClient extends BaseNetworkClient {

	private static final Logger		LOGGER				= LoggerFactory.getLogger(NetworkClient.class);

	private LoadBalancerFactory		loadBalancerFactory	= null;
	private volatile LoadBalancer	loadBalancer		= null;

	public NetworkClient(NetworkClientConfig config, LoadBalancerFactory loadBalancerFactory) {
		super(config);
		this.loadBalancerFactory = loadBalancerFactory;
	}

	public Future<Object> sendMessage(Object message) {

		if (message == null) {
			throw new IllegalArgumentException("message is null");
		}

		checkIfConnected();

		verifyMessageRegistered(message);

		if (loadBalancer == null) {
			throw new NoNodesAvailableException(String.format("No node available that can handle the message: %s", message));
		}

		Node node = loadBalancer.nextNode();
		if (node == null) {
			throw new NoNodesAvailableException(String.format("No node available that can handle the message: %s", message));
		}

		final ResponseFuture future = new ResponseFuture();

		doSendMessage(node, message, new Closure() {

			@Override
			public void execute(Object message) {
				future.offerResponse(message);
			}
		});

		return future;
	}

	@Override
	public void updateLoadBalancer(Set<Node> nodes) {
		if (nodes != null && nodes.size() > 0) {
			try {
				loadBalancer = loadBalancerFactory.newLoadBalancer(nodes);
			} catch (Exception ex) {
				String msg = "Exception while creating new router instance";
				LOGGER.error(msg, ex);
				throw new InvalidClusterException(msg, ex);
			}
		}

	}
}
