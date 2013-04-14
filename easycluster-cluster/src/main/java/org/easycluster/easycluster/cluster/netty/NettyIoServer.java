package org.easycluster.easycluster.cluster.netty;

import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.easycluster.easycluster.cluster.NetworkDefaults;
import org.easycluster.easycluster.cluster.exception.NetworkingException;
import org.easycluster.easycluster.cluster.server.ClusterIoServer;
import org.jboss.netty.bootstrap.ServerBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelException;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.channel.socket.DefaultServerSocketChannelConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class NettyIoServer implements ClusterIoServer {

	private static final Logger	LOGGER				= LoggerFactory.getLogger(NettyIoServer.class);

	private int									maxRetryCount	= NetworkDefaults.MAX_RETRY_COUNT;
	private long								retryTimeOut	= NetworkDefaults.RETRY_TIMEOUT_MILLIS;

	private ServerBootstrap			bootstrap			= null;
	private ChannelGroup				channelGroup	= null;
	private Channel							serverChannel	= null;

	public NettyIoServer(ServerBootstrap bootstrap, ChannelGroup channelGroup) {
		this.bootstrap = bootstrap;
		this.channelGroup = channelGroup;
	}

	@Override
	public void bind(int port) {

		SocketAddress address = new InetSocketAddress(port);

		int retryCount = 0;
		boolean binded = false;
		do {
			try {
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("Binding server socket to {}", address);
				}
				serverChannel = bootstrap.bind(address);
				binded = true;
			} catch (ChannelException ex) {
				LOGGER.warn("start failed : " + ex + ", and retry...");

				retryCount++;
				if (retryCount >= maxRetryCount) {
					throw new NetworkingException(String.format("Unable to bind to %s", port), ex);
				}
				try {
					Thread.sleep(retryTimeOut);
				} catch (InterruptedException e1) {
				}
			}
		} while (!binded);

		DefaultServerSocketChannelConfig config = (DefaultServerSocketChannelConfig) (serverChannel.getConfig());
		config.setBacklog(10240);
		config.setReuseAddress(true);
		config.setReceiveBufferSize(1024);

		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("binded to port:" + port);
		}
	}

	@Override
	public void shutdown() {
		if (null != serverChannel) {
			serverChannel.close().awaitUninterruptibly();
		}
		channelGroup.close().awaitUninterruptibly();
		bootstrap.releaseExternalResources();
	}

	public void setMaxRetryCount(int maxRetryCount) {
		this.maxRetryCount = maxRetryCount;
	}

	public void setRetryTimeOut(long retryTimeOut) {
		this.retryTimeOut = retryTimeOut;
	}

}
