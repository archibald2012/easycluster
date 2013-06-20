/**
 * 
 */
package org.easycluster.easycluster.cluster.manager;

import java.io.ByteArrayInputStream;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Unmarshaller;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.common.SystemUtil;
import org.easycluster.easycluster.cluster.exception.SerializeException;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.CoreEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ClusterNotification {

	private static final Logger			LOGGER				= LoggerFactory.getLogger(ClusterNotification.class);

	private String						serviceName			= null;
	private Set<Node>					currentNodes		= new HashSet<Node>();
	private Map<Long, ClusterListener>	listeners			= new HashMap<Long, ClusterListener>();
	private boolean						connected			= false;
	private Long						listenerId			= 0L;

	private JAXBContext					clusterEventcontext	= null;

	public ClusterNotification(String serviceName) {
		this.serviceName = serviceName;

		try {
			clusterEventcontext = JAXBContext.newInstance(ClusterEvent.class);
		} catch (JAXBException e) {
			String error = "Failed to ininitialize JAXB with error " + e.getMessage();
			LOGGER.error(error);
			throw new RuntimeException(error, e);
		}
	}

	public Long handleAddListener(ClusterListener listener) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling AddListener({}) message", listener);
		}

		listenerId += 1;
		listeners.put(listenerId, listener);
		if (connected) {
			listener.handleClusterConnected(getAvailableNodes());
		}
		return listenerId;
	}

	public void handleRemoveListener(Long key) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling RemoveListener({}) message", key);
		}
		ClusterListener listener = listeners.remove(key);
		if (listener == null) {
			LOGGER.info("Attempt to remove an unknown listener with key: {}", key);
		}
	}

	public void handleConnected(Collection<Node> nodes) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling Connected({}) message", nodes);
		}

		if (connected) {
			LOGGER.error("Received a Connected event when already connected");
		} else {
			connected = true;
			currentNodes.addAll(nodes);

			for (ClusterListener listener : listeners.values()) {
				listener.handleClusterConnected(getAvailableNodes());
			}
		}
	}

	public void handleDisconnected() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling Disconnected message");
		}

		if (connected) {
			connected = false;
			currentNodes = new HashSet<Node>();

			for (ClusterListener listener : listeners.values()) {
				listener.handleClusterDisconnected();
			}
		} else {
			LOGGER.error("Received a Disconnected event when disconnected");
		}
	}

	public void handleNodesChanged(Collection<Node> nodes) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling NodesChanged({}) message", nodes);
		}

		if (connected) {
			currentNodes.clear();
			currentNodes.addAll(nodes);

			for (ClusterListener listener : listeners.values()) {
				listener.handleClusterNodesChanged(getAvailableNodes());
			}
		} else {
			LOGGER.error("Received a NodesChanged event when disconnected");
		}
	}

	public void handleClusterEvent(String clusterEvent) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling cluster event ({})", clusterEvent);
		}

		Object request = null;
		try {
			Unmarshaller unmarshaller = clusterEventcontext.createUnmarshaller();
			request = unmarshaller.unmarshal(new ByteArrayInputStream(clusterEvent.getBytes()));
		} catch (JAXBException e) {
			String error = "Ignored the request with JAXB error " + e.getMessage();
			LOGGER.warn(error, e);
			throw new SerializeException(error, e);
		}

		CoreEvent event = ((ClusterEvent) request).getEvent();

		// check the destination of this event
		boolean isTargeted = true;
		if (event.getHostName() != null) {
			if (!event.getHostName().equals(SystemUtil.getHostName())) {
				isTargeted = false;
			}
		}
		if (event.getPid() != null) {
			if (!event.getPid().equals(SystemUtil.getPid())) {
				isTargeted = false;
			}
		}
		if (event.getServiceName() != null) {
			if (!event.getServiceName().equals(serviceName)) {
				isTargeted = false;
			}
		}
		if (!isTargeted) {
			LOGGER.debug("Event " + event + " is not targeted to this instance.");
			return;
		}

		if (connected) {
			for (ClusterListener listener : listeners.values()) {
				listener.handleClusterEvent((ClusterEvent) request);
			}
		} else {
			LOGGER.error("Received a cluster event when disconnected");
		}
	}

	public void handleShutdown() {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling Shutdown message");
		}
		for (ClusterListener listener : listeners.values()) {
			listener.handleClusterShutdown();
		}
		currentNodes = new HashSet<Node>();

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("ClusterNotification shut down");
		}
	}

	public Set<Node> getAvailableNodes() {
		Set<Node> available = new HashSet<Node>();
		for (Node node : currentNodes) {
			if (node.getAvailable()) {
				available.add(node);
			}
		}
		return Collections.unmodifiableSet(available);
	}

	public Set<Node> getCurrentNodes() {
		return Collections.unmodifiableSet(currentNodes);
	}

}
