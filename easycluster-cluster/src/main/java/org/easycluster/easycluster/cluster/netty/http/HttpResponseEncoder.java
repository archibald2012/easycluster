/**
 * 
 */
package org.easycluster.easycluster.cluster.netty.http;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseEncoder implements Transformer<Object, HttpResponse> {

	private static final Logger				LOGGER	= LoggerFactory.getLogger(HttpResponseEncoder.class);

	private Transformer<XipSignal, byte[]>	bytesEncoder;

	@Override
	public HttpResponse transform(Object from) {
		DefaultHttpResponse resp = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);

		resp.setStatus(HttpResponseStatus.OK);
		resp.setHeader(HttpHeaders.Names.CONTENT_TYPE, "application/x-tar");

		byte[] bytes = null;
		if (from instanceof XipSignal) {
			bytes = bytesEncoder.transform((XipSignal) from);
		} else if (from instanceof byte[]) {
			bytes = (byte[]) from;
		}

		if (null != bytes) {
			resp.setContent(ChannelBuffers.wrappedBuffer(bytes));
			resp.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("encoded http response: [{}]", resp);
		}

		return resp;
	}

	public void setBytesEncoder(Transformer<XipSignal, byte[]> bytesEncoder) {
		this.bytesEncoder = bytesEncoder;
	}

}
