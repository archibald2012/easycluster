package org.easycluster.easycluster.cluster.netty.endpoint;

import org.jboss.netty.channel.Channel;

public interface EndpointFactory {

	Endpoint createEndpoint(Channel channel);

	void setEndpointListener(IEndpointListener endpointListener);
}
