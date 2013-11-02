
package org.easycluster.easycluster.http;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class ConstantResponse {
	private ConstantResponse() {
	}

	public static final DefaultHttpResponse	RESPONSE_200_NOBODY;

	public static final DefaultHttpResponse	RESPONSE_400_NOBODY;

	public static final DefaultHttpResponse	RESPONSE_SERVER_BUSY;

	static {
		RESPONSE_200_NOBODY = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		RESPONSE_200_NOBODY.setContent(ChannelBuffers.wrappedBuffer("OK".getBytes()));

		RESPONSE_400_NOBODY = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.BAD_REQUEST);

		RESPONSE_SERVER_BUSY = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.SERVICE_UNAVAILABLE);
		RESPONSE_SERVER_BUSY.setContent(ChannelBuffers.wrappedBuffer("Server Too Busy".getBytes()));
	}

	public static HttpResponse get200NobodyResponse() {
		return RESPONSE_200_NOBODY;
	}

	public static HttpResponse get400NobodyResponse() {
		return RESPONSE_400_NOBODY;
	}

	public static HttpResponse getResponseServerBusy() {
		return RESPONSE_SERVER_BUSY;
	}

	public static HttpResponse get200WithContentTypeResponse(String contentType) {
		DefaultHttpResponse response200WithContentType = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.OK);
		response200WithContentType.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
		return response200WithContentType;
	}
}
