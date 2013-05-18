package org.easycluster.easycluster.cluster.netty.endpoint;

import org.easycluster.easycluster.core.IpPortPair;
import org.easycluster.easycluster.core.Sender;

public interface Endpoint extends Sender {

	void start();

	void stop();

	boolean isConnected();

	IpPortPair getRemoteAddress();
	
	void close();
}
