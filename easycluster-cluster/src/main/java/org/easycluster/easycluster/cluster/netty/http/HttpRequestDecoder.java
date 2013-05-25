package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.cluster.netty.codec.ByteBeanDecoder;
import org.easycluster.easycluster.core.TransportUtil;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestDecoder extends OneToOneDecoder {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpRequestDecoder.class);

	private ByteBeanDecoder		byteBeanDecoder	= new ByteBeanDecoder();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {
		if (msg instanceof HttpRequest) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("receive http request: [{}]", msg);
			}

			ChannelBuffer content = ((HttpRequest) msg).getContent();
			if (null != content) {
				Object signal = byteBeanDecoder.transform(content, channel);
				TransportUtil.attachRequest(signal, msg);
				return signal;
			}
		}
		return null;
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
