package org.easycluster.easycluster.cluster.netty;

import java.net.InetSocketAddress;

import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.group.DefaultChannelGroup;

public class ChannelPoolFactory {

	private ClientBootstrap	bootstrap;
	private int				maxConnections;
	private int				writeTimeoutMillis;

	public ChannelPoolFactory(ClientBootstrap bootstrap, int maxConnections, int writeTimeoutMillis) {
		this.bootstrap = bootstrap;
		this.maxConnections = maxConnections;
		this.writeTimeoutMillis = writeTimeoutMillis;
	}

	public ChannelPool newChannelPool(InetSocketAddress address) {
		ChannelGroup group = new DefaultChannelGroup(String.format("netty-client [%s]", address));
		return new ChannelPool(address, maxConnections, writeTimeoutMillis, bootstrap, group);
	}

	public void shutdown() {
		bootstrap.releaseExternalResources();
	}
}
