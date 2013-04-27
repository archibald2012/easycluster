package org.easycluster.easycluster.cluster.netty.endpoint;

import java.net.InetSocketAddress;

import org.easycluster.easycluster.cluster.common.IpPortPair;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.core.Closure;
import org.jboss.netty.channel.Channel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultEndpoint implements Endpoint {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(DefaultEndpoint.class);

	private Channel channel = null;
	private IEndpointListener endpointListener = null;

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
	public void send(Object message) {
		if (message != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("send - [{}]", message);
			}
			channel.write(new MessageContext(message));
		}
	}

	@Override
	public void send(Object message, Closure arg1) {
		throw new UnsupportedOperationException("not implemented yet!");
	}

	@Override
	public IpPortPair getRemoteAddress() {
		InetSocketAddress addr = (InetSocketAddress) channel.getRemoteAddress();
		return new IpPortPair(addr.getHostName(), addr.getPort());
	}

	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

	@Override
	public boolean isConnected() {
		return channel != null && channel.isConnected();
	}

}
