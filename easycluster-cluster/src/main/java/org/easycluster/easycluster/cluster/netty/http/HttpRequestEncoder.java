package org.easycluster.easycluster.cluster.netty.http;

import java.util.UUID;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestEncoder implements Transformer<Object, HttpRequest> {

	private static final Logger				LOGGER	= LoggerFactory.getLogger(HttpRequestEncoder.class);

	private Transformer<XipSignal, byte[]>	bytesEncoder;

	@Override
	public HttpRequest transform(Object message) {
		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");

		request.setHeader("uuid", UUID.randomUUID());

		byte[] bytes = null;
		if (message instanceof XipSignal) {
			bytes = bytesEncoder.transform((XipSignal) message);
		} else if (message instanceof byte[]) {
			bytes = (byte[]) message;
		}

		if (bytes != null) {
			request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
			// request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
			// "application/json");
			request.setContent(ChannelBuffers.wrappedBuffer(bytes));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("encoded http request: [{}]", request);
		}

		return request;
	}

	public void setBytesEncoder(Transformer<XipSignal, byte[]> bytesEncoder) {
		this.bytesEncoder = bytesEncoder;
	}
}
