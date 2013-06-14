package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestDecoder implements Transformer<HttpRequest, Object> {

	@SuppressWarnings("unused")
	private static final Logger				LOGGER	= LoggerFactory.getLogger(HttpRequestDecoder.class);

	private Transformer<byte[], XipSignal>	bytesDecoder;

	@Override
	public Object transform(HttpRequest request) {

		ChannelBuffer content = request.getContent();
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes(bytes);
		XipSignal signal = bytesDecoder.transform(bytes);
		return signal;

	}

	public void setBytesDecoder(Transformer<byte[], XipSignal> bytesDecoder) {
		this.bytesDecoder = bytesDecoder;
	}

}
