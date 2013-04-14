package org.easycluster.easycluster.cluster.netty.endpoint;

import org.jboss.netty.channel.Channel;

public class DefaultEndpointFactory implements EndpointFactory {

	private IEndpointListener	endpointListener	= null;

	@Override
	public Endpoint createEndpoint(Channel channel) {
		DefaultEndpoint endpoint = new DefaultEndpoint(channel);
		endpoint.setEndpointListener(endpointListener);
		endpoint.start();
		return endpoint;
	}

	@Override
	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointListener = endpointListener;
	}

}
