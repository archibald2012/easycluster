package org.easycluster.easycluster.cluster.manager;

public interface ClusterClientMBean {
	String[] getClusterNodes();

	boolean isCusterConnected();
}
