package org.easycluster.easycluster.cluster.manager;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.exception.ClusterDisconnectedException;
import org.easycluster.easycluster.cluster.exception.ClusterNotStartedException;
import org.easycluster.easycluster.cluster.exception.ClusterShutdownException;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.EventHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultClusterClient implements ClusterClient {

	private static final Logger		LOGGER				= LoggerFactory.getLogger(DefaultClusterClient.class);

	private ClusterNotification		clusterNotification	= null;
	private ClusterManager			clusterManager		= null;
	private String					serviceGroup		= null;
	private String					service				= null;
	private AtomicBoolean			shutdownSwitch		= new AtomicBoolean(false);
	private AtomicBoolean			startedSwitch		= new AtomicBoolean(false);
	private EventHandler			clusterEventHandler	= null;
	private volatile CountDownLatch	connectedLatch		= new CountDownLatch(1);
	private String					mbeanObjectName		= "org.easycluster:type=DefaultClusterClient,service=%s";

	public DefaultClusterClient(String serviceGroup, String service) {
		this.serviceGroup = serviceGroup;
		this.service = service;
		this.clusterNotification = new ClusterNotification(service);

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName measurementName = new ObjectName(String.format(mbeanObjectName, service));
			if (mbeanServer.isRegistered(measurementName)) {
				mbeanServer.unregisterMBean(measurementName);
			}
			StandardMBean clusterClientMBean = new StandardMBean(new ClusterClientMBean() {
				public String[] getClusterNodes() {
					Set<Node> nodes = getNodes();
					List<String> ret = new ArrayList<String>();
					for (Node node : nodes) {
						ret.add(node.toString());
					}
					return ret.toArray(new String[] {});
				}

				public boolean isCusterConnected() {
					return isConnected();
				}
			}, ClusterClientMBean.class);

			mbeanServer.registerMBean(clusterClientMBean, measurementName);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Registering with JMX server as MBean [" + measurementName + "]");
			}
		} catch (Exception e) {
			String message = "Unable to register MBeans with error " + e.getMessage();
			LOGGER.error(message, e);
		}
	}

	@Override
	public void start() {
		if (shutdownSwitch.get()) {
			throw new ClusterShutdownException();
		}

		if (startedSwitch.compareAndSet(false, true)) {

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Starting ClusterClient...");
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Starting ClusterManager...");
			}
			clusterManager.start();

			addListener(new ClusterListener() {

				public void handleClusterConnected(Set<Node> nodes) {
					connectedLatch.countDown();
				}

				public void handleClusterNodesChanged(Set<Node> nodes) {

				}

				public void handleClusterDisconnected() {
					connectedLatch = new CountDownLatch(1);
				}

				public void handleClusterShutdown() {

				}

				@Override
				public void handleClusterEvent(ClusterEvent event) {
					if (clusterEventHandler != null) {
						clusterEventHandler.handleClusterEvent(event);
					}
				}
			});

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Cluster started");
			}
		}
	}

	@Override
	public String getServiceGroup() {
		return serviceGroup;
	}

	@Override
	public String getService() {
		return service;
	}

	@Override
	public Set<Node> getNodes() throws ClusterDisconnectedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (!isConnected()) {
			throw new ClusterDisconnectedException();
		}
		return clusterNotification.getCurrentNodes();
	}

	@Override
	public Node getNodeWithId(String nodeId) throws ClusterDisconnectedException {
		Set<Node> nodes = getNodes();
		Node node = null;
		for (Node n : nodes) {
			if (n.getId().equals(nodeId)) {
				node = n;
				break;
			}
		}
		return node;
	}

	@Override
	public Node addNode(Node node) throws ClusterDisconnectedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (!isConnected()) {
			throw new ClusterDisconnectedException();
		}
		if (node == null) {
			throw new IllegalArgumentException("node is null");
		}

		clusterManager.addNode(node);
		return node;
	}

	@Override
	public void removeNode(String nodeId) throws ClusterDisconnectedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (!isConnected()) {
			throw new ClusterDisconnectedException();
		}

		clusterManager.removeNode(nodeId);
	}

	@Override
	public void markNodeAvailable(String nodeId) throws ClusterDisconnectedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (!isConnected()) {
			throw new ClusterDisconnectedException();
		}

		clusterManager.markNodeAvailable(nodeId);
	}

	@Override
	public void markNodeUnavailable(String nodeId) throws ClusterDisconnectedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (!isConnected()) {
			throw new ClusterDisconnectedException();
		}

		clusterManager.markNodeUnavailable(nodeId);
	}

	@Override
	public Long addListener(ClusterListener listener) {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (listener == null) {
			throw new IllegalArgumentException("Listener is null.");
		}

		return clusterNotification.handleAddListener(listener);
	}

	@Override
	public void removeListener(Long key) {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		if (key == null) {
			throw new IllegalArgumentException("Listener key is null.");
		}

		clusterNotification.handleRemoveListener(key);
	}

	@Override
	public boolean isShutdown() {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		return shutdownSwitch.get();
	}

	@Override
	public boolean isConnected() {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		return !isShutdown() && connectedLatch.getCount() == 0;
	}

	@Override
	public void awaitConnection() throws InterruptedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}

		connectedLatch.await();
	}

	@Override
	public boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}

		return connectedLatch.await(timeout, unit);
	}

	@Override
	public void awaitConnectionUninterruptibly() {
		if (!startedSwitch.get()) {
			throw new ClusterNotStartedException();
		}
		if (isShutdown()) {
			throw new ClusterShutdownException();
		}
		boolean completed = false;

		while (!completed) {
			try {
				awaitConnection();
				completed = true;
			} catch (InterruptedException e) {
				// do nothing
			}
		}
	}

	@Override
	public void shutdown() {
		if (shutdownSwitch.compareAndSet(false, true)) {

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Shutting down clusterClient...");
			}

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Shutting down ClusterManager...");
			}
			clusterManager.shutdown();

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Cluster client shut down");
			}
		}
	}

	public void setClusterManager(ClusterManager clusterManager) {
		this.clusterManager = clusterManager;
	}

	public void setClusterEventHandler(EventHandler clusterEventHandler) {
		this.clusterEventHandler = clusterEventHandler;
	}

	public ClusterNotification getClusterNotification() {
		return clusterNotification;
	}

}
