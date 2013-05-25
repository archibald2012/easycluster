package org.easycluster.easycluster.cluster.netty.http;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;

public interface HttpResponseSender {

	void sendResponse(Channel channel, HttpResponse response);

	void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus);

	void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent);

	void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent, String charsetName, String contentType);

	void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent, String charsetName);

	void sendRedirectResponse(Channel channel, String redirectUrl);

	String sendFile(Channel channel, byte[] fullContent, int startPos, int endPos);
}
