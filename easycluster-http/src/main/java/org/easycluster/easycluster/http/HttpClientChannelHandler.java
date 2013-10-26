package org.easycluster.easycluster.http;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.core.KeyTransformer;
import org.easycluster.easycluster.core.Transformer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpClientChannelHandler extends SimpleChannelHandler {

	private static final Logger					LOGGER					= LoggerFactory.getLogger(HttpClientChannelHandler.class);

	private MessageContextHolder				messageContextHolder	= null;
	private KeyTransformer						keyTransformer			= new HttpKeyTransformer();
	private Transformer<Object, HttpRequest>	requestTransformer		= null;
	private Transformer<HttpResponse, Object>	responseTransformer		= null;

	public HttpClientChannelHandler(MessageContextHolder messageContextHolder) {
		this.messageContextHolder = messageContextHolder;
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		if (e.getMessage() instanceof MessageContext) {

			MessageContext requestContext = (MessageContext) e.getMessage();

			HttpRequest request = requestTransformer.transform(requestContext.getMessage());

			Object requestId = keyTransformer.transform(request);
			messageContextHolder.add(requestId, requestContext);

			Channels.write(ctx, e.getFuture(), request);
		} else {
			super.writeRequested(ctx, e);
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Object message = e.getMessage();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Received message: {}", message);
		}

		Object requestId = keyTransformer.transform(message);

		Object signal = responseTransformer.transform((HttpResponse) message);

		MessageContext requestContext = messageContextHolder.remove(requestId, signal);

		if (requestContext != null) {
			requestContext.getClosure().execute(signal);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("Caught exception in network layer", e.getCause());
		}
	}

	public void setRequestTransformer(Transformer<Object, HttpRequest> requestTransformer) {
		this.requestTransformer = requestTransformer;
	}

	public void setResponseTransformer(Transformer<HttpResponse, Object> responseTransformer) {
		this.responseTransformer = responseTransformer;
	}

}
