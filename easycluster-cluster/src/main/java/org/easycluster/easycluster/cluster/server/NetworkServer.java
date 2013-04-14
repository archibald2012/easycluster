package org.easycluster.easycluster.cluster.server;

import java.lang.reflect.Method;
import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.easycluster.easycluster.cluster.ClusterDefaults;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.common.ClassUtil;
import org.easycluster.easycluster.cluster.exception.ClusterException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;
import org.easycluster.easycluster.cluster.exception.NetworkServerNotBoundException;
import org.easycluster.easycluster.cluster.exception.NetworkShutdownException;
import org.easycluster.easycluster.cluster.exception.NetworkingException;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.ClusterListener;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.zookeeper.ZooKeeperClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkServer {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NetworkServer.class);

	protected String applicationName = null;
	protected String serviceName = null;
	protected ClusterClient clusterClient = null;
	protected MessageClosureRegistry messageClosureRegistry = new MessageClosureRegistry();

	protected ClusterIoServer clusterIoServer = null;
	protected MessageExecutor messageExecutor = null;

	protected boolean markAvailableWhenConnected = true;
	protected String version = null;
	protected int port = -1;
	protected int[] partitionIds = new int[0];
	protected String url = null;

	private AtomicBoolean shutdownSwitch = new AtomicBoolean(false);
	private volatile Node node = null;
	private volatile Long listenerKey = null;

	public NetworkServer(String applicationName, String serviceName,
			String zooKeeperConnectString) {
		this(applicationName, serviceName, zooKeeperConnectString,
				ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS);
	}

	public NetworkServer(String applicationName, String serviceName,
			String zooKeeperConnectString, int zooKeeperSessionTimeoutMillis) {
		this.applicationName = applicationName;
		this.serviceName = serviceName;
		this.clusterClient = new ZooKeeperClusterClient(applicationName,
				serviceName, zooKeeperConnectString,
				zooKeeperSessionTimeoutMillis);
	}

	/**
	 * Registers a message handler with the <code>NetworkServer</code>. The
	 * <code>NetworkServer</code> will call the provided handler when an
	 * incoming request of type <code>requestMessage</code> is received. If a
	 * response is expected then a response message should also be provided.
	 * 
	 * @param requestMessage
	 *            an incoming request message
	 * @param responseMessage
	 *            an outgoing response message
	 * @param handler
	 *            the function to call when an incoming message of type
	 *            <code>requestMessage</code> is received
	 */
	public void registerHandler(Class<?> requestMessage,
			Class<?> responseMessage, MessageClosure<?, ?> handler) {
		LOGGER.info(
				"registerHandler request=[{}], response=[{}], handler=[{}]",
				new Object[] { requestMessage, responseMessage, handler });

		messageClosureRegistry.registerHandler(requestMessage, responseMessage,
				handler);
	}

	/**
	 * Binds the network server instance to the wildcard address and the port of
	 * the <code>Node</code> identified by the provided nodeId and automatically
	 * marks the <code>Node</code> available in the cluster. A <code>Node</code>
	 * 's url must be specified in the format hostname:port.
	 * 
	 * @param nodeId
	 *            the id of the <code>Node</code> this server is associated
	 *            with.
	 * 
	 * @throws InvalidNodeException
	 *             thrown if no <code>Node</code> with the specified
	 *             <code>nodeId</code> exists
	 * @throws NetworkingException
	 *             thrown if unable to bind
	 */
	public void bind(int nodeId) {
		bind(nodeId, new int[0], true);
	}

	/**
	 * Binds the network server instance to the wildcard address and the port of
	 * the <code>Node</code> identified by the provided nodeId and marks the
	 * <code>Node</code> available in the cluster if <code>markAvailable</code>
	 * is true. A <code>Node</code>'s url must be specified in the format
	 * hostname:port.
	 * 
	 * @param nodeId
	 *            the id of the <code>Node</code> this server is associated
	 *            with.
	 * @param markAvailable
	 *            if true marks the <code>Node</code> identified by
	 *            <code>nodeId</code> as available after binding to the port
	 * 
	 * @throws InvalidNodeException
	 *             thrown if no <code>Node</code> with the specified
	 *             <code>nodeId</code> exists or if the format of the
	 *             <code>Node</code>'s url isn't hostname:port
	 * @throws NetworkingException
	 *             thrown if unable to bind
	 */
	public void bind(final int nodeId, final int[] partitionIds,
			final boolean markAvailable) {
		if (shutdownSwitch.get()) {
			throw new NetworkShutdownException("");
		}
		if (node != null) {
			throw new NetworkingException(
					"Attempt to bind an already bound NetworkServer");
		}

		LOGGER.info("Starting NetworkServer...");

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Ensuring ClusterClient is started");
		}
		clusterClient.start();
		clusterClient.awaitConnectionUninterruptibly();

		Node node = new Node(nodeId, new InetSocketAddress(nodeId), false);
		node.setVersion(version);
		node.setUrl(url);
		clusterClient.addNode(node);

		node = clusterClient.getNodeWithId(nodeId);
		if (node == null) {
			throw new InvalidNodeException("No node with id " + nodeId
					+ " exists");
		}

		clusterIoServer.bind(nodeId);

		this.node = node;

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Registering with ClusterClient");
		}

		listenerKey = clusterClient.addListener(new ClusterListener() {

			@Override
			public void handleClusterConnected(Set<Node> nodes) {
				if (markAvailable) {
					if (LOGGER.isDebugEnabled()) {
						LOGGER.debug("Marking node with id " + nodeId
								+ " available");
					}
					try {
						clusterClient.markNodeAvailable(nodeId);
					} catch (ClusterException ex) {
						LOGGER.error("Unable to mark node available", ex);
					}
				}
			}

			@Override
			public void handleClusterNodesChanged(Set<Node> nodes) {
			}

			@Override
			public void handleClusterDisconnected() {
				// TODO reconnect to cluster?
			}

			@Override
			public void handleClusterShutdown() {
				doShutdown(true);
			}

			@Override
			public void handleClusterEvent(ClusterEvent event) {

			}

		});

		LOGGER.info("NetworkServer started");
	}

	/**
	 * Returns the <code>Node</code> associated with this server.
	 * 
	 * @return the <code>Node</code> associated with this server
	 */
	public Node getMyNode() {
		if (shutdownSwitch.get()) {
			throw new NetworkShutdownException();
		}
		if (node == null) {
			throw new NetworkServerNotBoundException();
		}
		return node;
	}

	/**
	 * Marks the node available in the cluster if the server is bound.
	 */
	public void markAvailable() {
		clusterClient.markNodeAvailable(getMyNode().getId());
	}

	/**
	 * Marks the node unavailable in the cluster if bound.
	 */
	public void markUnavailable() {
		clusterClient.markNodeUnavailable(getMyNode().getId());
	}

	/**
	 * Shuts down the network server. This results in unbinding from the port,
	 * closing the child sockets, and marking the node unavailable.
	 */
	public void stop() {
		doShutdown(false);
	}

	private void doShutdown(boolean fromCluster) {
		if (shutdownSwitch.compareAndSet(false, true)) {

			String nodeString = (node == null) ? "[unbound]" : node.toString();
			LOGGER.info("Shutting down NetworkServer for {}...", nodeString);

			if (!fromCluster) {
				if (nodeString != null) {
					try {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Unregistering from ClusterClient");
						}
						clusterClient.removeListener(listenerKey);

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Marking {} unavailable", nodeString);
						}
						clusterClient.markNodeUnavailable(node.getId());

						clusterClient.shutdown();
					} catch (ClusterShutdownException ex) {
						// cluster already shut down, ignore
					}
				}
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Closing opened sockets");
			}
			clusterIoServer.shutdown();

			LOGGER.info("NetworkServer shut down");
		}
	}

	public void setHandlers(ArrayList<MessageClosure<?, ?>> handlers) {
		for (MessageClosure<?, ?> handler : handlers) {
			Method[] methods = ClassUtil.getAllMethodOf(handler.getClass());
			for (Method method : methods) {
				if (method.getName().equals("execute")) {
					Class<?>[] params = method.getParameterTypes();
					if (params.length < 1) {
						continue;
					}

					Class<?> requestType = params[0];
					Class<?> responseType = method.getReturnType();
					responseType = (Void.class.isAssignableFrom(responseType)) ? null
							: responseType;

					registerHandler(requestType, responseType, handler);
				}
			}
		}
	}

	public void setClusterIoServer(ClusterIoServer clusterIoServer) {
		this.clusterIoServer = clusterIoServer;
	}

	public void setMessageClosureRegistry(
			MessageClosureRegistry messageClosureRegistry) {
		this.messageClosureRegistry = messageClosureRegistry;
	}

	public void setMessageExecutor(MessageExecutor messageExecutor) {
		this.messageExecutor = messageExecutor;
	}

	public void setMarkAvailableWhenConnected(boolean markAvailableWhenConnected) {
		this.markAvailableWhenConnected = markAvailableWhenConnected;
	}

	public void setVersion(String version) {
		this.version = version;
	}

	public void setUrl(String url) {
		this.url = url;
	}

}
