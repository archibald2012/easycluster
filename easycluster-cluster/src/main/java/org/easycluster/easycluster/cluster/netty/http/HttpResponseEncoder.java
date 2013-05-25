/**
 * 
 */
package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.codec.ByteBeanEncoder;
import org.easycluster.easycluster.core.TransportUtil;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.oneone.OneToOneEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseEncoder extends OneToOneEncoder {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpResponseEncoder.class);

	private ByteBeanEncoder		byteBeanEncoder	= new ByteBeanEncoder();

	@Override
	protected Object encode(ChannelHandlerContext ctx, Channel channel, Object msg) throws Exception {

		DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		resp.setStatus(HttpResponseStatus.OK);
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-tar");

		MessageContext context = (MessageContext) msg;
		Object message = context.getMessage();

		byte[] bytes = null;
		if (message instanceof XipSignal) {
			bytes = byteBeanEncoder.transform((XipSignal) message);
		} else if (message instanceof byte[]) {
			bytes = (byte[]) message;
		}

		if (null != bytes) {
			resp.setContent(ChannelBuffers.wrappedBuffer(bytes));
			resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		}

		HttpRequest req = (HttpRequest) TransportUtil.getRequestOf(message);
		if (req != null) {
			String uuid = req.getHeader("uuid");
			if (uuid != null) {
				resp.setHeader("uuid", uuid);
			}

			// 是否需要持久连接
			String keepAlive = req.getHeader(HttpHeaders.Names.CONNECTION);
			if (keepAlive != null) {
				resp.setHeader(HttpHeaders.Names.CONNECTION, keepAlive);
			}
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("encoded http response: [{}]", resp);
		}

		return resp;
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
