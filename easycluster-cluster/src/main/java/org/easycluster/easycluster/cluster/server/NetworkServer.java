package org.easycluster.easycluster.cluster.server;

import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.cluster.NetworkServerConfig;
import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.exception.ClusterException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;
import org.easycluster.easycluster.cluster.exception.NetworkShutdownException;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.ClusterListener;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.zookeeper.ZooKeeperClusterClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NetworkServer {

	private static final Logger			LOGGER					= LoggerFactory.getLogger(NetworkServer.class);

	private ClusterClient				clusterClient			= null;
	private Node						node					= null;
	private AtomicBoolean				shutdownSwitch			= new AtomicBoolean(false);
	private volatile Long				listenerKey				= null;

	protected MessageClosureRegistry	messageClosureRegistry	= new MessageClosureRegistry();
	protected ClusterIoServer			clusterIoServer			= null;

	public NetworkServer(final NetworkServerConfig config) {

		this.node = new Node(config.getIp(), config.getPort(), Arrays.asList(config.getPartitions()));
		node.setServiceGroup(config.getServiceGroup());
		node.setService(config.getService());
		node.setVersion(config.getVersion());
		node.setUrl(config.getUrl());

		this.clusterClient = new ZooKeeperClusterClient(config.getServiceGroup(), config.getService(), config.getZooKeeperConnectString(),
				config.getZooKeeperSessionTimeoutMillis());
	}

	public void start() {

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Starting NetworkServer...");
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Ensuring ClusterClient is started");
		}

		clusterClient.start();
		clusterClient.awaitConnectionUninterruptibly();

		clusterClient.addNode(node);

		final String nodeId = node.getId();

		if (clusterClient.getNodeWithId(nodeId) == null) {
			throw new InvalidNodeException("No node with id " + nodeId + " exists");
		}

		clusterIoServer.bind(node.getPort());

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Registering with ClusterClient");
		}

		listenerKey = clusterClient.addListener(new ClusterListener() {

			@Override
			public void handleClusterConnected(Set<Node> nodes) {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Marking node with id " + nodeId + " available");
				}
				try {
					clusterClient.markNodeAvailable(nodeId);
				} catch (ClusterException ex) {
					LOGGER.error("Unable to mark node available", ex);
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

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("NetworkServer started");
		}
	}

	public void stop() {
		doShutdown(false);
	}

	public void registerHandler(Class<?> requestMessage, Class<?> responseMessage, MessageClosure<?, ?> handler) {
		String responseType = (responseMessage == null) ? null : responseMessage.getName();
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("registerHandler request=[{}], response=[{}], handler=[{}]", new Object[] { requestMessage.getName(), responseType,
					handler.getClass().getName() });
		}
		messageClosureRegistry.registerHandler(requestMessage, responseMessage, handler);
	}

	public Node getMyNode() {
		if (shutdownSwitch.get()) {
			throw new NetworkShutdownException();
		}
		return node;
	}

	public void markAvailable() {
		clusterClient.markNodeAvailable(getMyNode().getId());
	}

	public void markUnavailable() {
		clusterClient.markNodeUnavailable(getMyNode().getId());
	}

	private void doShutdown(boolean fromCluster) {
		if (shutdownSwitch.compareAndSet(false, true)) {

			String nodeString = node.toString();
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Shutting down NetworkServer for {}...", node);
			}

			if (!fromCluster) {
				if (listenerKey != null) {
					try {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Unregistering from ClusterClient");
						}
						clusterClient.removeListener(listenerKey);

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Marking {} unavailable", nodeString);
						}
						clusterClient.markNodeUnavailable(node.getId());

						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Remove node {}", node.getId());
						}
						clusterClient.removeNode(node.getId());

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

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("NetworkServer shut down");
			}
		}
	}

	public void setHandlers(List<MessageClosure<?, ?>> handlers) {
		if (handlers != null) {
			for (MessageClosure<?, ?> handler : handlers) {
				Method[] methods = getAllMethodOf(handler.getClass());
				for (Method method : methods) {
					if (method.getName().equals("execute")) {
						Class<?>[] params = method.getParameterTypes();
						if (params.length < 1) {
							continue;
						}

						Class<?> requestType = params[0];
						Class<?> responseType = method.getReturnType();
						responseType = (Void.class.isAssignableFrom(responseType)) ? null : responseType;

						registerHandler(requestType, responseType, handler);
					}
				}
			}
		}
	}

	private Method[] getAllMethodOf(final Class<?> clazz) {
		Method[] methods = null;

		Class<?> itr = clazz;
		while (!itr.equals(Object.class) && !itr.isInterface()) {
			methods = (Method[]) ArrayUtils.addAll(itr.getDeclaredMethods(), methods);
			itr = itr.getSuperclass();
		}

		return methods;
	}

}
