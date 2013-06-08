package org.easycluster.easycluster.cluster.netty.tcp;

import org.easycluster.easycluster.core.Transformer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class NettyBeanDecoder extends LengthFieldBasedFrameDecoder {

	private Transformer<ChannelBuffer, Object>	byteDecoder;

	public NettyBeanDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		return byteDecoder.transform(buffer);
	}

	public void setByteDecoder(Transformer<ChannelBuffer, Object> byteDecoder) {
		this.byteDecoder = byteDecoder;
	}

}
