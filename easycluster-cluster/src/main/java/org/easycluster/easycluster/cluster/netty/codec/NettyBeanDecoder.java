package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.LengthFieldBasedFrameDecoder;

public class NettyBeanDecoder extends LengthFieldBasedFrameDecoder {

	private ByteBeanDecoder	byteBeanDecoder	= new ByteBeanDecoder();

	public NettyBeanDecoder(int maxFrameLength, int lengthFieldOffset, int lengthFieldLength, int lengthAdjustment, int initialBytesToStrip) {
		super(maxFrameLength, lengthFieldOffset, lengthFieldLength, lengthAdjustment, initialBytesToStrip);
	}

	public NettyBeanDecoder() {
		// maxLength 1M
		super(1024 * 1024, 0, 4, 0, 0);
	}

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {
		return byteBeanDecoder.transform(buffer, channel);
	}

	public void setByteBeanDecoder(ByteBeanDecoder byteBeanDecoder) {
		this.byteBeanDecoder = byteBeanDecoder;
	}

	public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
		byteBeanDecoder.setTypeMetaInfo(typeMetaInfo);
	}

	public void setDumpBytes(int dumpBytes) {
		byteBeanDecoder.setDumpBytes(dumpBytes);
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		byteBeanDecoder.setDebugEnabled(isDebugEnabled);
	}

	public void setEncryptKey(String encryptKey) {
		byteBeanDecoder.setEncryptKey(encryptKey);
	}

}
