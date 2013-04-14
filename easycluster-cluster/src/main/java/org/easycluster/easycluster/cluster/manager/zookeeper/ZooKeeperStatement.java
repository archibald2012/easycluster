package org.easycluster.easycluster.cluster.manager.zookeeper;

import org.apache.zookeeper.KeeperException;
import org.apache.zookeeper.ZooKeeper;

public interface ZooKeeperStatement {
	void doInZooKeeper(ZooKeeper zk) throws KeeperException, InterruptedException;
}
