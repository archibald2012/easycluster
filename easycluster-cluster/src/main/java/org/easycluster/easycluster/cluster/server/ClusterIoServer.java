package org.easycluster.easycluster.cluster.server;

public interface ClusterIoServer {

	/**
	 * 
	 * @param port
	 */
	void bind(int port);

	/**
	 * 
	 */
	void shutdown();
}
