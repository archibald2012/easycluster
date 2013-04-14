package org.easycluster.easycluster.cluster.manager.memory;

import org.easycluster.easycluster.cluster.manager.DefaultClusterClient;

public class InMemoryClusterClient extends DefaultClusterClient {

	public InMemoryClusterClient(String applicationName, String serviceName) {
		super(applicationName, serviceName);
		InMemoryClusterManager clusterManager = new InMemoryClusterManager(
				serviceName);
		clusterManager.setClusterNotification(getClusterNotification());
		setClusterManager(clusterManager);
	}

}
