package org.easycluster.easycluster.monitor.service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.Node;
import org.easycluster.easycluster.cluster.manager.ClusterClient;
import org.easycluster.easycluster.cluster.manager.zookeeper.ZooKeeperClusterClient;
import org.easycluster.easycluster.monitor.domain.ServerStatus;
import org.easycluster.easycluster.monitor.domain.ServerStore;

public class ClusterService {

	private ServerStore					serverStore				= null;
	private ScheduledExecutorService	exec					= Executors.newSingleThreadScheduledExecutor();
	private int							checkInterval			= 1000;
	private String						serviceGroup			= null;
	private String[]					serviceNames			= new String[0];
	private String						zooKeeperConnectString	= null;

	public void start() {

		final List<ClusterClient> clusterClients = new ArrayList<ClusterClient>();
		for (String serviceName : serviceNames) {
			ClusterClient clusterClient = new ZooKeeperClusterClient(serviceGroup, serviceName, zooKeeperConnectString);
			clusterClient.start();
			clusterClients.add(clusterClient);
		}

		exec.scheduleWithFixedDelay(new Runnable() {

			@Override
			public void run() {

				for (ClusterClient clusterClient : clusterClients) {
					Set<Node> nodes = clusterClient.getNodes();

					for (Node node : nodes) {
						ServerStatus server = new ServerStatus();
						server.setDomain(node.getServiceGroup());
						server.setGroup(node.getService());
						server.setIp(node.getHostName());
						server.setPort(node.getPort());
						server.setVersion(node.getVersion());
						server.setAvailable(node.getAvailable());
						serverStore.refreshServers(clusterClient.getServiceGroup(), clusterClient.getService(), server);
					}
				}
			}
		}, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		exec.shutdownNow();
	}

	public void setServerStore(ServerStore serverStore) {
		this.serverStore = serverStore;
	}

	public void setCheckInterval(int checkInterval) {
		this.checkInterval = checkInterval;
	}

	public void setServiceGroup(String serviceGroup) {
		this.serviceGroup = serviceGroup;
	}

	public void setServiceNames(String serviceNames) {
		String[] temp = serviceNames.split(",");
		this.serviceNames = new String[temp.length];
		for (int i = 0; i < temp.length; i++) {
			this.serviceNames[i] = temp[i].trim();
		}
	}

	public void setZooKeeperConnectString(String zooKeeperConnectString) {
		this.zooKeeperConnectString = zooKeeperConnectString;
	}

}
