package org.easycluster.easycluster.cluster.manager.zookeeper;

import org.easycluster.easycluster.cluster.ClusterDefaults;
import org.easycluster.easycluster.cluster.manager.DefaultClusterClient;

public class ZooKeeperClusterClient extends DefaultClusterClient {

	public ZooKeeperClusterClient(String applicationName, String serviceName,
			String zooKeeperConnectString) {
		this(applicationName, serviceName, zooKeeperConnectString,
				ClusterDefaults.ZOOKEEPER_SESSION_TIMEOUT_MILLIS);
	}

	public ZooKeeperClusterClient(String applicationName, String serviceName,
			String zooKeeperConnectString, int zooKeeperSessionTimeoutMillis) {
		super(applicationName, serviceName);
		ZooKeeperClusterManager clusterManager = new ZooKeeperClusterManager(
				serviceName, zooKeeperConnectString,
				zooKeeperSessionTimeoutMillis);
		clusterManager.setClusterNotification(getClusterNotification());
		setClusterManager(clusterManager);
	}

}
