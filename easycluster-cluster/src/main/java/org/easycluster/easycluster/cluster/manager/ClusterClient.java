package org.easycluster.easycluster.cluster.manager;

import java.util.Set;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.exception.ClusterDisconnectedException;
import org.easycluster.easycluster.cluster.exception.InvalidNodeException;


public interface ClusterClient {

	/**
	 * Retrieves the name of the application running on this cluster
	 * 
	 * @return the name of the application running on this cluster
	 */
	String getApplicationName();

	/**
	 * Retrieves the name of the service running on this cluster
	 * 
	 * @return the name of the service running on this cluster
	 */
	String getServiceName();

	/**
	 * Retrieves the current list of nodes registered with the cluster.
	 * 
	 * @return the current list of nodes
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is not connected when the method is called
	 */
	Set<Node> getNodes() throws ClusterDisconnectedException;

	/**
	 * Looks up the node with the specified id.
	 * 
	 * @param nodeId
	 *          the id of the node to find
	 * 
	 * @return the node if found, otherwise null
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is not connected when the method is called
	 */
	Node getNodeWithId(int nodeId) throws ClusterDisconnectedException;

	/**
	 * Adds a node to the cluster metadata.
	 * 
	 * @param node
	 *          the node to add
	 * 
	 * @return the newly added node
	 * @throws InvalidNodeException
	 *           thrown if there is an error adding the new node to the cluster metadata
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is disconnected when the method is called
	 */
	Node addNode(Node node) throws ClusterDisconnectedException;

	/**
	 * Removes a node from the cluster metadata.
	 * 
	 * @param nodeId
	 *          the id of the node to remove
	 * 
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is disconnected when the method is called
	 */
	void removeNode(int nodeId) throws ClusterDisconnectedException;

	/**
	 * Marks a cluster node as online and available for receiving requests.
	 * 
	 * @param nodeId
	 *          the id of the node to mark available
	 * 
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is disconnected when the method is called
	 */
	void markNodeAvailable(int nodeId) throws ClusterDisconnectedException;

	/**
	 * Marks a cluster node as offline and unavailable for receiving requests.
	 * 
	 * @param nodeId
	 *          the id of the node to mark unavailable
	 * 
	 * @throws ClusterDisconnectedException
	 *           thrown if the cluster is disconnected when the method is called
	 */
	void markNodeUnavailable(int nodeId) throws ClusterDisconnectedException;

	/**
	 * Registers a <code>ClusterListener</code> with the <code>ClusterClient</code> to receive cluster events.
	 * 
	 * @param listener
	 *          the listener instance to register
	 * 
	 * @return a ClusterListenerKey that can be used to unregister the listener
	 */
	Long addListener(ClusterListener listener);

	/**
	 * Unregisters a <code>ClusterListener</code> with the <code>ClusterClient</code>.
	 * 
	 * @param key
	 *          the key what was returned by <code>addListener</code> when the <code>ClusterListener</code> was registered
	 */
	void removeListener(Long key);

	/**
	 * Queries whether or not a connection to the cluster is established.
	 * 
	 * @return true if connected, false otherwise
	 */
	boolean isConnected();

	/**
	 * Queries whether or not this <code>ClusterClient</code> has been shut down.
	 * 
	 * @return true if shut down, false otherwise
	 */
	boolean isShutdown();

	/**
	 * Waits for the connection to the cluster to be established. This method will wait indefinitely for the connection.
	 * 
	 * @throws InterruptedException
	 *           thrown if the current thread is interrupted while waiting
	 */
	void awaitConnection() throws InterruptedException;

	/**
	 * Waits for the connection to the cluster to be established for the specified duration of time.
	 * 
	 * @param timeout
	 *          how long to wait before giving up, in terms of <code>unit</code>
	 * @param unit
	 *          the <code>TimeUnit</code> that <code>timeout</code> should be interpreted in
	 * 
	 * @return true if the connection was established before the timeout, false if the timeout occurred
	 * @throws InterruptedException
	 *           thrown if the current thread is interrupted while waiting
	 */
	boolean awaitConnection(long timeout, TimeUnit unit) throws InterruptedException;

	/**
	 * Waits for the connection to the cluster to be established. This method will wait indefinitely for the connection
	 * and will swallow any <code>InterruptedException</code>s thrown while waiting.
	 */
	void awaitConnectionUninterruptibly();

	/**
	 * start up the cluster client.
	 */
	void start();

	/**
	 * Shut down the cluster client.
	 */
	void shutdown();
}
