package org.easycluster.easycluster.cluster.netty.tcp;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class NettyBeanEncoder extends OneToOneEncoder {

	private Transformer<XipSignal, byte[]>	bytesEncoder;

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object message) throws Exception {
		MessageContext context = (MessageContext) message;
		Object request = context.getMessage();
		if (request instanceof XipSignal) {
			byte[] bytes = bytesEncoder.transform((XipSignal) request);
			return ChannelBuffers.wrappedBuffer(bytes);
		}
		return request;
	}

	public void setBytesEncoder(Transformer<XipSignal, byte[]> bytesEncoder) {
		this.bytesEncoder = bytesEncoder;
	}

}
