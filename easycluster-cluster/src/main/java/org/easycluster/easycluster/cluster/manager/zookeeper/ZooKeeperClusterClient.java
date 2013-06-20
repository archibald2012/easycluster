package org.easycluster.easycluster.cluster.manager.zookeeper;

import org.easycluster.easycluster.cluster.manager.DefaultClusterClient;

public class ZooKeeperClusterClient extends DefaultClusterClient {

	public ZooKeeperClusterClient(String serviceGroup, String service, String zooKeeperConnectString, int zooKeeperSessionTimeoutMillis) {
		super(serviceGroup, service);
		ZooKeeperClusterManager clusterManager = new ZooKeeperClusterManager(serviceGroup, service, zooKeeperConnectString, zooKeeperSessionTimeoutMillis);
		clusterManager.setClusterNotification(getClusterNotification());
		setClusterManager(clusterManager);
	}

}
