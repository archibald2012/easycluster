package org.easycluster.easycluster.websocket.endpoint;

import org.easycluster.easycluster.cluster.netty.endpoint.DefaultEndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.Endpoint;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointFactory;
import org.jboss.netty.channel.Channel;

public class WebSocketEndpointFactory extends DefaultEndpointFactory implements EndpointFactory {

	@Override
	public Endpoint createEndpoint(Channel channel) {
		WebSocketEndpoint endpoint = new WebSocketEndpoint(channel);
		endpoint.setEndpointListener(endpointListener);
		endpoint.start();
		return endpoint;
	}
}
