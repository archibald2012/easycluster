/**
 * 
 */
package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;

public class BinaryWebSocketFrameEncoder extends OneToOneEncoder {

	private ByteBeanEncoder	byteBeanEncoder = new ByteBeanEncoder();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		MessageContext context = (MessageContext) msg;
		Object request = context.getMessage();
		if (request instanceof XipSignal) {
			BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
			byte[] bytes = byteBeanEncoder.transform((XipSignal) request);
			if (null != bytes) {
				frame.setBinaryData(ChannelBuffers.wrappedBuffer(bytes));
			}
			return frame;
		}
		return request;
	}

	public void setByteBeanEncoder(ByteBeanEncoder byteBeanEncoder) {
		this.byteBeanEncoder = byteBeanEncoder;
	}

}
