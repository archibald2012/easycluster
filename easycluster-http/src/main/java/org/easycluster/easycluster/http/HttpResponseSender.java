package org.easycluster.easycluster.http;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;

public class HttpResponseSender {

	public void sendResponse(Channel channel, HttpResponse response) {
		ChannelFuture future = channel.write(response);
		if (!HttpHeaders.isKeepAlive(response) || !response.containsHeader(HttpHeaders.Names.CONTENT_LENGTH)) {
			// no content
			future.addListener(ChannelFutureListener.CLOSE);
		}
	}

	public void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent) throws UnsupportedEncodingException {
		sendResponse(channel, httpResponseStatus, responseContent, "UTF-8");
	}

	public void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent, String charsetName)
			throws UnsupportedEncodingException {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
		byte[] contents = responseContent.getBytes(charsetName);
		response.setContent(ChannelBuffers.wrappedBuffer(contents));
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, contents.length);
		sendResponse(channel, response);
	}

	public String sendFile(Channel channel, byte[] fullContent, int startPos, int endPos) {
		HttpResponseStatus httpResponseStatus = (startPos > 0 || endPos > 0) ? HttpResponseStatus.PARTIAL_CONTENT : HttpResponseStatus.OK;
		if (startPos < 0 || startPos > fullContent.length)
			startPos = 0;
		if (endPos < startPos || endPos > fullContent.length || endPos <= 0)
			endPos = fullContent.length;

		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
		byte[] partialContent = Arrays.copyOfRange(fullContent, startPos, endPos + 1);
		response.setContent(ChannelBuffers.wrappedBuffer(partialContent));
		response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, partialContent.length);
		String range = "bytes " + startPos + "-" + endPos + "/" + fullContent.length;
		response.setHeader(HttpHeaders.Names.CONTENT_RANGE, range);
		sendResponse(channel, response);

		return httpResponseStatus.equals(HttpResponseStatus.PARTIAL_CONTENT) ? range : null;
	}

	public void sendNotFoundResponse(Channel channel) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.NOT_FOUND);
		sendResponse(channel, response);
	}

	public void sendGatewayTimeoutResponse(Channel channel) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.GATEWAY_TIMEOUT);
		sendResponse(channel, response);
	}

	public void sendRedirectResponse(Channel channel, String redirectUrl) {
		HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, HttpResponseStatus.TEMPORARY_REDIRECT);
		response.setHeader(HttpHeaders.Names.LOCATION, redirectUrl);
		sendResponse(channel, response);
	}
}
