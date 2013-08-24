package org.easycluster.easycluster.cluster.netty;

import java.lang.management.ManagementFactory;
import java.net.InetSocketAddress;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.exception.ChannelPoolClosedException;
import org.jboss.netty.bootstrap.ClientBootstrap;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.group.ChannelGroup;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Channel pool management.
 * 
 * 
 */
public class ChannelPool {

	private static final Logger				LOGGER				= LoggerFactory.getLogger(ChannelPool.class);

	private InetSocketAddress				address				= null;
	private ClientBootstrap					bootstrap			= null;
	private ChannelGroup					channelGroup		= null;
	private int								maxConnections		= -1;
	private int								writeTimeoutMillis	= -1;
	private BlockingQueue<Channel>			pool				= null;
	private BlockingQueue<MessageContext>	pendingWrites		= null;
	private AtomicInteger					poolSize			= new AtomicInteger(0);
	private AtomicBoolean					closed				= new AtomicBoolean(false);
	private AtomicInteger					requestsSent		= new AtomicInteger(0);

	private String							mbeanObjectName		= "org.easycluster:type=ChannelPool,hostName=%s,port=%s";

	public ChannelPool(final InetSocketAddress address, final int maxConnections, final int writeTimeoutMillis, final ClientBootstrap bootstrap,
			final ChannelGroup channelGroup) {
		this.address = address;
		this.maxConnections = maxConnections;
		this.writeTimeoutMillis = writeTimeoutMillis;
		this.bootstrap = bootstrap;
		this.channelGroup = channelGroup;

		pool = new ArrayBlockingQueue<Channel>(maxConnections);
		pendingWrites = new LinkedBlockingQueue<MessageContext>();

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName measurementName = new ObjectName(String.format(mbeanObjectName, address.getHostName(), address.getPort()));
			if (mbeanServer.isRegistered(measurementName)) {
				mbeanServer.unregisterMBean(measurementName);
			}
			StandardMBean channelPoolMBean = new StandardMBean(new ChannelPoolMBean() {
				public int getOpenChannels() {
					return poolSize.get();
				}

				public int getMaxChannels() {
					return maxConnections;
				}

				public int getWriteQueueSize() {
					return pendingWrites.size();
				}

				public int getNumberRequestsSent() {
					return requestsSent.get();
				}
			}, ChannelPoolMBean.class);

			mbeanServer.registerMBean(channelPoolMBean, measurementName);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Registering with JMX server as MBean [" + measurementName + "]");
			}
		} catch (Exception e) {
			String message = "Unable to register MBeans with error " + e.getMessage();
			LOGGER.error(message, e);
		}
	}

	public void sendRequest(MessageContext request) {
		if (closed.get()) {
			throw new ChannelPoolClosedException("ChannelPool is already closed!");
		}

		Channel channel = getPooledChannel();

		if (channel == null) {
			pendingWrites.offer(request);
			openChannel();
		} else {
			writeRequestToChannel(request, channel);
			flushPendingMessages(channel);
		}
	}

	private Channel getPooledChannel() {
		boolean found = false;
		Channel channel = null;

		while (!pool.isEmpty() && !found) {
			Channel c = pool.poll();
			if (c == null) {
				// do nothing
			} else {
				if (c.isConnected()) {
					channel = c;
					found = true;
				} else {
					poolSize.decrementAndGet();
				}
			}
		}
		return channel;
	}

	private void openChannel() {

		if (poolSize.incrementAndGet() > maxConnections) {
			poolSize.decrementAndGet();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Unable to open channel, pool is full");
			}
		} else {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Opening a channel to: {}", address);
			}

			bootstrap.connect(address).addListener(new ChannelFutureListener() {
				public void operationComplete(ChannelFuture openFuture) {
					if (openFuture.isSuccess()) {
						Channel channel = openFuture.getChannel();
						channelGroup.add(channel);
						flushPendingMessages(channel);
					} else {
						LOGGER.error(String.format("Error when opening channel to: %s", address), openFuture.getCause());
						poolSize.decrementAndGet();
					}
				}
			});
		}
	}

	private void flushPendingMessages(Channel channel) {
		while (!pendingWrites.isEmpty()) {
			MessageContext request = pendingWrites.poll();
			if (request == null) {
				// do nothing
			} else {
				if (((System.nanoTime() - request.getTimestamp()) / 1000000) < writeTimeoutMillis)
					writeRequestToChannel(request, channel);
				else {
					request.getClosure().execute(new TimeoutException("Timed out while waiting to write"));
				}
			}
		}

		pool.offer(channel);
	}

	private void writeRequestToChannel(final MessageContext request, final Channel channel) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Writing to {}: {}", channel, request);
		}

		requestsSent.incrementAndGet();

		channel.write(request).addListener(new ChannelFutureListener() {
			public void operationComplete(ChannelFuture writeFuture) {
				if (!writeFuture.isSuccess()) {
					LOGGER.error("Writing to {} failed: {}", channel, writeFuture.getCause());
					request.getClosure().execute(writeFuture.getCause());
				}
			}
		});
	}

	public void close() {
		if (closed.compareAndSet(false, true)) {
			channelGroup.close().awaitUninterruptibly();
		}
	}
}
