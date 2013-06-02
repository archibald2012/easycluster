package org.easycluster.easycluster.cluster.server;

public interface ClusterIoServer {

	void bind(int port);

	void shutdown();
}
