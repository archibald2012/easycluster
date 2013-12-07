package org.easycluster.easycluster.websocket;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.NettyConstants;
import org.easycluster.easycluster.cluster.serialization.Serialization;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TextWebSocketFrameEncoder implements Transformer<XipSignal, TextWebSocketFrame> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(TextWebSocketFrameEncoder.class);

	private Serialization		serialization	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;

	@Override
	public TextWebSocketFrame transform(XipSignal signal) {

		SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
		if (null == attr) {
			throw new InvalidMessageException("invalid signal, no messageCode defined.");
		}

		ChannelBuffer buffer = ChannelBuffers.dynamicBuffer();
		byte[] bytes = serialization.serialize(signal);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and signal raw bytes --> {}", ToStringBuilder.reflectionToString(signal),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		String header = NettyConstants.HEADER_UUID + ":" + signal.getIdentification() + "\r\n\r\n";
		header += NettyConstants.MSG_CODE + ":" + attr.messageCode() + "\r\n\r\n";

		buffer.writeBytes(header.getBytes(CharsetUtil.UTF_8));
		if (null != bytes) {
			buffer.writeBytes(bytes);
		}
		TextWebSocketFrame frame = new TextWebSocketFrame(buffer);
		return frame;
	}

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
	}

}
