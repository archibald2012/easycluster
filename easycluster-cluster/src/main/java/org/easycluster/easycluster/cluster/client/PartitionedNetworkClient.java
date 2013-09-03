package org.easycluster.easycluster.cluster.client;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.Future;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.client.loadbalancer.PartitionedLoadBalancer;
import org.easycluster.easycluster.cluster.client.loadbalancer.PartitionedLoadBalancerFactory;
import org.easycluster.easycluster.cluster.common.DefaultResponseIterator;
import org.easycluster.easycluster.cluster.common.ResponseFuture;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.exception.InvalidClusterException;
import org.easycluster.easycluster.cluster.exception.NoNodesAvailableException;
import org.easycluster.easycluster.core.Closure;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PartitionedNetworkClient<PartitionedId> extends BaseNetworkClient {

	private static final Logger								LOGGER				= LoggerFactory.getLogger(PartitionedNetworkClient.class);

	private PartitionedLoadBalancerFactory<PartitionedId>	loadBalancerFactory	= null;
	private volatile PartitionedLoadBalancer<PartitionedId>	loadBalancer;

	public PartitionedNetworkClient(NetworkClientConfig config, PartitionedLoadBalancerFactory<PartitionedId> loadBalancerFactory) {
		super(config);
		this.loadBalancerFactory = loadBalancerFactory;
	}

	public Future<Object> sendMessage(PartitionedId id, Object message) {

		if (id == null) {
			throw new IllegalArgumentException("Partition id is null");
		}
		if (message == null) {
			throw new IllegalArgumentException("Message is null");
		}

		checkIfConnected();

		verifyMessageRegistered(message);

		Node node = loadBalancer.nextNode(id);
		if (node == null) {
			throw new NoNodesAvailableException(String.format("Unable to satisfy request, no node available for id [%s], message: [%s]", id, message));
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

	public ResponseIterator sendMessage(Set<PartitionedId> ids, Object message) {

		if (ids == null) {
			throw new IllegalArgumentException("Partition ids is null");
		}
		if (message == null) {
			throw new IllegalArgumentException("Message is null");
		}

		checkIfConnected();

		verifyMessageRegistered(message);

		Set<Node> nodes = calculateNodesFromIds(ids);

		final DefaultResponseIterator it = new DefaultResponseIterator(currentNodes.size());

		for (Node node : nodes) {

			Object obj = message;
			if (message instanceof AbstractXipSignal) {
				try {
					obj = ((AbstractXipSignal) message).clone();
				} catch (CloneNotSupportedException e) {
					LOGGER.error("Clone operation not supported!", e);
				}
			}

			doSendMessage(node, obj, new Closure() {

				@Override
				public void execute(Object message) {
					it.offerResponse(message);
				}
			});
		}

		return it;
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

	public Node nextNode(PartitionedId id) {
		return loadBalancer == null ? null : loadBalancer.nextNode(id);
	}

	private Set<Node> calculateNodesFromIds(Set<PartitionedId> ids) {
		Set<Node> ret = new HashSet<Node>();
		for (PartitionedId id : ids) {
			Node node = nextNode(id);
			if (node == null) {
				throw new NoNodesAvailableException(String.format("Unable to satisfy request, no node available for id %s", id));
			}
			ret.add(node);
		}
		return ret;
	}
}
