package org.easycluster.easycluster.websocket;

import java.util.Map;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.NettyConstants;
import org.easycluster.easycluster.cluster.serialization.Serialization;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketFrameDecoder implements Transformer<WebSocketFrame, Object> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(WebSocketFrameDecoder.class);

	private Int2TypeMetainfo	typeMetaInfo	= null;
	private Serialization		serialization	= null;

	@Override
	public Object transform(WebSocketFrame frame) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("transform [{}]", frame);
		}

		ChannelBuffer buffer = frame.getBinaryData();

		Map<String, String> headers = WebSocketHeaderUtil.parseHeader(buffer);
		if (headers == null || headers.isEmpty()) {
			if (LOGGER.isErrorEnabled()) {
				LOGGER.error("Can't parser header, the frame: {}", frame);
			}
			return null;
		}
		String requestCode = headers.get(NettyConstants.MSG_CODE);

		int messageCode = Integer.parseInt(requestCode);

		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknown message code:" + messageCode);
		}

		byte[] bytes = new byte[buffer.readableBytes()];
		buffer.readBytes(bytes);

		XipSignal signal = (XipSignal) serialization.deserialize(bytes, type);

		return signal;
	}

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
	}

	public void setTypeMetaInfo(Int2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

}
