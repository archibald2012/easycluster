package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseDecoder implements Transformer<HttpResponse, Object> {

	@SuppressWarnings("unused")
	private static final Logger				LOGGER	= LoggerFactory.getLogger(HttpResponseDecoder.class);

	private Transformer<byte[], XipSignal>	bytesDecoder;

	@Override
	public Object transform(HttpResponse from) {
		if (from.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
			return null;
		}

		ChannelBuffer content = from.getContent();
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes(bytes);
		XipSignal signal = bytesDecoder.transform(bytes);
		return signal;

	}

	public void setBytesDecoder(Transformer<byte[], XipSignal> bytesDecoder) {
		this.bytesDecoder = bytesDecoder;
	}
}
