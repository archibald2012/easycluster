package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.cluster.netty.codec.ByteBeanDecoder;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseDecoder extends OneToOneDecoder {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpResponseDecoder.class);

	private ByteBeanDecoder		byteBeanDecoder	= new ByteBeanDecoder();

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

//		if (LOGGER.isDebugEnabled()) {
//			LOGGER.debug("decode: [{}]", msg);
//		}
		
		if (msg instanceof HttpResponse) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("receive http response: [{}]", msg);
			}

			HttpResponse response = (HttpResponse) msg;
			if (response.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
				return msg;
			}

			ChannelBuffer content = response.getContent();
			if (null != content) {
				return byteBeanDecoder.transform(content);
			}
		}
		return msg;
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
