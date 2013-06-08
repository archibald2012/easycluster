package org.easycluster.easycluster.cluster.netty.codec;

import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public interface ProtocolCodecFactory {

	ChannelDownstreamHandler getEncoder();

	ChannelUpstreamHandler getDecoder();
}
