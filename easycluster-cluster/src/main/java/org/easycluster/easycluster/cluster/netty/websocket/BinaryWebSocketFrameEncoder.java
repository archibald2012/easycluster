///**
// * 
// */
//package org.easycluster.easycluster.cluster.netty.websocket;
//
//import org.easycluster.easycluster.cluster.common.MessageContext;
//import org.easycluster.easycluster.cluster.netty.codec.ByteBeanEncoder;
//import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
//import org.jboss.netty.buffer.ChannelBuffers;
//import org.jboss.netty.channel.Channel;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
//import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class BinaryWebSocketFrameEncoder extends OneToOneEncoder {
//
//	private static final Logger	LOGGER			= LoggerFactory.getLogger(BinaryWebSocketFrameEncoder.class);
//
//	private ByteBeanEncoder		byteBeanEncoder	= new ByteBeanEncoder();
//
//	@Override
//	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
//		if (msg instanceof MessageContext) {
//			MessageContext context = (MessageContext) msg;
//			Object request = context.getMessage();
//			if (request instanceof XipSignal) {
//				BinaryWebSocketFrame frame = new BinaryWebSocketFrame();
//				byte[] bytes = byteBeanEncoder.transform((XipSignal) request);
//				if (null != bytes) {
//					frame.setBinaryData(ChannelBuffers.wrappedBuffer(bytes));
//				}
//
//				if (LOGGER.isDebugEnabled()) {
//					LOGGER.debug("encoded websocket frame: [{}]", frame);
//				}
//
//				return frame;
//			}
//		}
//		return msg;
//	}
//
//	public void setByteBeanEncoder(ByteBeanEncoder byteBeanEncoder) {
//		this.byteBeanEncoder = byteBeanEncoder;
//	}
//
//	public void setDumpBytes(int dumpBytes) {
//		byteBeanEncoder.setDumpBytes(dumpBytes);
//	}
//
//	public void setDebugEnabled(boolean isDebugEnabled) {
//		byteBeanEncoder.setDebugEnabled(isDebugEnabled);
//	}
//
//	public void setEncryptKey(String encryptKey) {
//		byteBeanEncoder.setEncryptKey(encryptKey);
//	}
//}
