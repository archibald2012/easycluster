package org.easycluster.easycluster.cluster.netty.tcp;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.codec.ByteBeanEncoder;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class NettyBeanEncoder extends OneToOneEncoder {

	private ByteBeanEncoder	byteBeanEncoder	= new ByteBeanEncoder();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object message) throws Exception {
		MessageContext context = (MessageContext) message;
		Object request = context.getMessage();
		if (request instanceof XipSignal) {
			byte[] bytes = byteBeanEncoder.transform((XipSignal) request);
			return ChannelBuffers.wrappedBuffer(bytes);
		}
		return request;
	}

	public void setByteBeanEncoder(ByteBeanEncoder byteBeanEncoder) {
		this.byteBeanEncoder = byteBeanEncoder;
	}

	public void setDumpBytes(int dumpBytes) {
		byteBeanEncoder.setDumpBytes(dumpBytes);
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		byteBeanEncoder.setDebugEnabled(isDebugEnabled);
	}

	public void setEncryptKey(String encryptKey) {
		byteBeanEncoder.setEncryptKey(encryptKey);
	}
}
