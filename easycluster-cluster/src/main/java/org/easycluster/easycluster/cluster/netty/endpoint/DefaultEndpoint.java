package org.easycluster.easycluster.cluster.netty.endpoint;

import java.net.InetSocketAddress;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.core.IpPortPair;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEndpoint implements Endpoint {

	private static final Logger	LOGGER				= LoggerFactory.getLogger(DefaultEndpoint.class);

	protected Channel				channel				= null;
	protected EndpointListener	endpointListener	= null;

	public DefaultEndpoint(Channel channel) {
		this.channel = channel;
	}

	@Override
	public void start() {
		if (endpointListener != null) {
			endpointListener.onCreate(this);
		}
	}

	@Override
	public void stop() {
		if (endpointListener != null) {
			endpointListener.onStop(this);
		}
	}

	@Override
	public void send(final Object message) {
		if (message != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("send - [{}]", message);
			}
			ChannelFuture future = channel.write(new MessageContext(message));
			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isDone()) {
						if (null != future.getCause()) {
							LOGGER.error("Send message failed, message [" + message + "], channel: [" + future.getChannel().getRemoteAddress() + "], cause: ",
									future.getCause());
						} else {
							LOGGER.error("Send message failed without reason, message: [" + message + "], channel: [" + future.getChannel().getRemoteAddress()
									+ "]");
						}

					}
				}
			});
		}
	}

	@Override
	public IpPortPair getRemoteAddress() {
		InetSocketAddress addr = (InetSocketAddress) channel.getRemoteAddress();
		return new IpPortPair(addr.getHostName(), addr.getPort());
	}

	@Override
	public void close() {
		if (this.channel != null) {
			this.channel.close();
		}
	}

	public void setEndpointListener(EndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

	@Override
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}

	@Override
	public String toString() {
		return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
	}

}
