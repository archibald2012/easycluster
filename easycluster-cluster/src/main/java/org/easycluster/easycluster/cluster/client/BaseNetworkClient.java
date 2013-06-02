package org.easycluster.easycluster.cluster.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easycluster.easycluster.cluster.NetworkClientConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.common.DefaultResponseIterator;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.common.ResponseFuture;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.exception.ClusterDisconnectedException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;
import org.easycluster.easycluster.cluster.exception.NetworkShutdownException;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.ClusterListener;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.zookeeper.ZooKeeperClusterClient;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseNetworkClient {

	private static final Logger		LOGGER			= LoggerFactory.getLogger(BaseNetworkClient.class);

	private AtomicBoolean			shutdownSwitch	= new AtomicBoolean(false);
	private ClusterClient			clusterClient	= null;
	private Long					listenerKey		= null;

	protected ClusterIoClient		clusterIoClient	= null;
	protected MessageRegistry		messageRegistry	= new MessageRegistry();
	protected volatile Set<Node>	currentNodes	= new HashSet<Node>();
	protected volatile boolean		connected		= false;

	public BaseNetworkClient(final NetworkClientConfig config) {
		clusterClient = new ZooKeeperClusterClient(config.getApplicationName(), config.getServiceName(), config.getZooKeeperConnectString(),
				config.getZooKeeperSessionTimeoutMillis());
	}

	public void start() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Ensuring cluster is started");
		}
		clusterClient.start();
		clusterClient.awaitConnectionUninterruptibly();

		connected = clusterClient.isConnected();

		listenerKey = clusterClient.addListener(new ClusterListener() {

			@Override
			public void handleClusterConnected(Set<Node> nodes) {
				updateCurrentState(nodes);
				connected = true;
			}

			@Override
			public void handleClusterNodesChanged(Set<Node> nodes) {
				updateCurrentState(nodes);
			}

			@Override
			public void handleClusterDisconnected() {
				connected = false;
				updateCurrentState(new HashSet<Node>());
			}

			@Override
			public void handleClusterShutdown() {
				doShutdown(true);
			}

			@Override
			public void handleClusterEvent(ClusterEvent event) {

			}

		});
	}

	public void stop() {
		doShutdown(false);
	}

	public void registerRequest(Class<?> requestMessage, Class<?> responseMessage) {
		messageRegistry.registerMessage(requestMessage, responseMessage);
	}

	public Future<Object> sendMessageToNode(Object message, Node node) {
		if (message == null) {
			throw new IllegalArgumentException("message is null");
		}
		if (node == null) {
			throw new IllegalArgumentException("node is null");
		}

		verifyMessageRegistered(message);

		Set<Node> candidate = new HashSet<Node>();
		for (Node n : currentNodes) {
			if (n.equals(node)) {
				candidate.add(n);
			}
		}

		if (candidate.size() == 0) {
			throw new InvalidNodeException(String.format("Unable to send message, %s is not available", node));
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

	public ResponseIterator broadcastMessage(Object message) {
		if (message == null) {
			throw new IllegalArgumentException("message is null");
		}
		verifyMessageRegistered(message);

		final DefaultResponseIterator it = new DefaultResponseIterator(currentNodes.size());

		for (Node node : currentNodes) {
			doSendMessage(node, message, new Closure() {

				@Override
				public void execute(Object message) {
					it.offerResponse(message);
				}
			});
		}

		return it;
	}

	private void doShutdown(boolean fromCluster) {
		if (shutdownSwitch.compareAndSet(false, true)) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Shutting down NetworkClient");
			}
			if (!fromCluster) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Unregistering from ClusterClient");
				}
				try {
					clusterClient.removeListener(listenerKey);
				} catch (ClusterShutdownException ex) {
					// cluster is already shut down
				}
			}
			if (clusterClient != null) {
				clusterClient.shutdown();
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Closing sockets");
			}

			clusterIoClient.shutdown();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("NetworkClient shut down");
			}
		}
	}

	private void updateCurrentState(Set<Node> nodes) {
		currentNodes = nodes;
		updateLoadBalancer(nodes);
		clusterIoClient.nodesChanged(nodes);
	}

	protected boolean verifyMessageRegistered(Object message) {
		if (!messageRegistry.contains(message)) {
			throw new InvalidMessageException(String.format("The message provided [%s] is not a registered request message", message));
		}
		return true;
	}

	protected void doSendMessage(Node node, Object message, Closure responseHandler) {
		clusterIoClient.sendMessage(node, message, responseHandler);
	}

	protected void checkIfConnected() {
		if (shutdownSwitch.get()) {
			throw new NetworkShutdownException();
		}
		if (!connected) {
			throw new ClusterDisconnectedException();
		}
	}

	protected void updateLoadBalancer(Set<Node> nodes) {

	}

	public void setMessageRegistry(MessageRegistry messageRegistry) {
		this.messageRegistry = messageRegistry;
	}

	public void setMessages(Map<Class<?>, Class<?>> messages) {
		for (Map.Entry<Class<?>, Class<?>> entry : messages.entrySet()) {
			registerRequest(entry.getKey(), entry.getValue());
		}
	}

	public void setClusterIoClient(ClusterIoClient clusterIoClient) {
		this.clusterIoClient = clusterIoClient;
	}

	public void setClusterClient(ClusterClient clusterClient) {
		this.clusterClient = clusterClient;
	}

	public ClusterClient getClusterClient() {
		return clusterClient;
	}

	public ClusterIoClient getClusterIoClient() {
		return clusterIoClient;
	}

}
