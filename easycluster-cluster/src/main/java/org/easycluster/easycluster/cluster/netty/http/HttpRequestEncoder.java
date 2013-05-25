package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.codec.ByteBeanEncoder;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 基于HTTP1.1
 * 
 * @author wangqi
 * @version $Id: HttpRequestEncoder.java 4 2012-01-10 11:51:54Z archie $
 */
public class HttpRequestEncoder extends OneToOneEncoder {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpRequestEncoder.class);

	private ByteBeanEncoder		byteBeanEncoder	= new ByteBeanEncoder();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");

		MessageContext context = (MessageContext) msg;
		Object message = context.getMessage();

		byte[] bytes = null;
		if (message instanceof XipSignal) {
			bytes = byteBeanEncoder.transform((XipSignal) message);
		} else if (message instanceof byte[]) {
			bytes = (byte[]) message;
		}

		if (bytes != null) {
			request.setHeader("Content-Length", bytes.length);
			request.setContent(ChannelBuffers.wrappedBuffer(bytes));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("encoded http request: [{}]", request);
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
