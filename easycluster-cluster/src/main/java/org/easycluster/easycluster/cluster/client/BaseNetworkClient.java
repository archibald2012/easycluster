package org.easycluster.easycluster.cluster.client;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easycluster.easycluster.cluster.ClusterDefaults;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.common.DefaultResponseIterator;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.common.ResponseFuture;
import org.easycluster.easycluster.cluster.common.ResponseIterator;
import org.easycluster.easycluster.cluster.exception.ClusterDisconnectedException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;
import org.easycluster.easycluster.cluster.exception.NetworkNotStartedException;
import org.easycluster.easycluster.cluster.exception.NetworkShutdownException;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.ClusterListener;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.zookeeper.ZooKeeperClusterClient;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseNetworkClient {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(BaseNetworkClient.class);

	protected String				applicationName			= null;
	protected String				serviceName				= null;
	private String					zooKeeperConnectString	= null;
	private int						zooKeeperSessionTimeoutMillis;
	protected MessageRegistry		messageRegistry			= new MessageRegistry();
	protected AtomicBoolean			startedSwitch			= new AtomicBoolean(false);
	protected AtomicBoolean			shutdownSwitch			= new AtomicBoolean(false);
	protected ClusterClient			clusterClient			= null;
	protected ClusterIoClient		clusterIoClient			= null;
	private Long					listenerKey				= null;
	protected volatile Set<Node>	currentNodes			= new HashSet<Node>();
	protected volatile boolean		connected				= false;

	public BaseNetworkClient(String applicationName, String serviceName, String zooKeeperConnectString) {
		this(applicationName, serviceName, zooKeeperConnectString, ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS);
	}

	public BaseNetworkClient(String applicationName, String serviceName, String zooKeeperConnectString, int zooKeeperSessionTimeoutMillis) {
		this.applicationName = applicationName;
		this.serviceName = serviceName;
		this.zooKeeperConnectString = zooKeeperConnectString;
		this.zooKeeperSessionTimeoutMillis = zooKeeperSessionTimeoutMillis;
	}

	public void start() {
		if (startedSwitch.compareAndSet(false, true)) {

			if (clusterClient == null) {
				clusterClient = new ZooKeeperClusterClient(applicationName, serviceName, zooKeeperConnectString, zooKeeperSessionTimeoutMillis);
			}

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
	}

	/**
	 * Registers a request/response message pair with the
	 * <code>NetworkClient</code>. Requests and their associated responses must
	 * be registered or an <code>InvalidMessageException</code> will be thrown
	 * when an attempt to send a <code>Message</code> is made.
	 * 
	 * @param requestMessage
	 *            an outgoing request message
	 * @param responseMessage
	 *            the expected response message or null if this is a one way
	 *            message
	 */
	public void registerRequest(Class<?> requestMessage, Class<?> responseMessage) {
		messageRegistry.registerMessage(requestMessage, responseMessage);
	}

	/**
	 * Sends a message to the specified node in the cluster.
	 * 
	 * @param message
	 *            the message to send
	 * @param node
	 *            the node to send the message to
	 * 
	 * @return a future which will become available when a response to the
	 *         message is received
	 * @throws InvalidNodeException
	 *             thrown if the node specified is not currently available
	 * @throws ClusterDisconnectedException
	 *             thrown if the cluster is not connected when the method is
	 *             called
	 */
	public Future<Object> sendMessageToNode(Object message, Node node) throws InvalidNodeException, ClusterDisconnectedException {
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

	/**
	 * Broadcasts a message to all the currently available nodes in the cluster.
	 * 
	 * @param message
	 *            the message to send
	 * 
	 * @return a <code>ResponseIterator</code> which will provide the responses
	 *         from the nodes in the cluster as they are received
	 * @throws ClusterDisconnectedException
	 *             thrown if the cluster is not connected when the method is
	 *             called
	 */
	public ResponseIterator broadcastMessage(Object message) throws ClusterDisconnectedException {
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

	/**
	 * Shuts down the <code>NetworkClient</code> and releases resources held.
	 */
	public void stop() {
		doShutdown(false);
	}

	private void doShutdown(boolean fromCluster) {
		if (shutdownSwitch.compareAndSet(false, true) && startedSwitch.get()) {
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
		if (!startedSwitch.get()) {
			throw new NetworkNotStartedException();
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

	public String getServiceName() {
		return serviceName;
	}

}
