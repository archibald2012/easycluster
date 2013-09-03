/**
 * 
 */
package org.easycluster.easycluster.cluster.manager;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.common.SystemUtil;
import org.easycluster.easycluster.cluster.common.XmlUtil;
import org.easycluster.easycluster.cluster.manager.event.ClusterEvent;
import org.easycluster.easycluster.cluster.manager.event.CoreEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class ClusterNotification {

	private static final Logger			LOGGER			= LoggerFactory.getLogger(ClusterNotification.class);

	private String						serviceName		= null;
	private Set<Node>					currentNodes	= new HashSet<Node>();
	private Map<Long, ClusterListener>	listeners		= new HashMap<Long, ClusterListener>();
	private boolean						connected		= false;
	private Long						listenerId		= 0L;

	public ClusterNotification(String serviceName) {
		this.serviceName = serviceName;
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
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Attempt to remove an unknown listener with key: {}", key);
			}
		}
	}

	public void handleConnected(Collection<Node> nodes) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Handling Connected({}) message", nodesAsString(nodes));
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
			LOGGER.debug("Handling NodesChanged({}) message", nodesAsString(nodes));
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
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Handling cluster event ({})", clusterEvent);
		}

		ClusterEvent request = XmlUtil.unmarshal(clusterEvent, ClusterEvent.class);

		CoreEvent event = request.getEvent();

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
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Event " + event + " is not targeted to this instance.");
			}
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

	private String nodesAsString(Collection<Node> nodes) {

		List<Node> nodeList = new ArrayList<Node>();
		nodeList.addAll(nodes);
		Collections.sort(nodeList);

		StringBuilder body = new StringBuilder();
		body.append("node num is:[");
		body.append(nodeList.size());
		body.append("]\r\n");
		for (Node n : nodeList) {
			body.append(n).append("\r\n");
		}
		return body.toString();
	}

}
