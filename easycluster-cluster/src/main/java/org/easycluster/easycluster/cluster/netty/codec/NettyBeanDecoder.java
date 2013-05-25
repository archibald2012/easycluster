package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;

public class NettyBeanDecoder extends OneToOneDecoder {

	private ByteBeanDecoder	byteBeanDecoder	= new ByteBeanDecoder();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object message) throws Exception {
		if (message instanceof ChannelBuffer) {
			return byteBeanDecoder.transform((ChannelBuffer) message, channel);
		}
		return message;
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
