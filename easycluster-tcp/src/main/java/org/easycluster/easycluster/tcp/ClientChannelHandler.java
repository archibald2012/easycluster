package org.easycluster.easycluster.tcp;

import org.easycluster.easycluster.cluster.common.AverageTracker;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannelHandler extends SimpleChannelHandler {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(ClientChannelHandler.class);

	private MessageContextHolder	messageContextHolder	= null;
	private KeyTransformer			keyTransformer			= new KeyTransformer();
	private AverageTracker			processingTime			= new AverageTracker(100);

	public ClientChannelHandler(MessageContextHolder messageContextHolder) {
		this.messageContextHolder = messageContextHolder;
	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Write requested message: {}", e.getMessage());
		}

		if (e.getMessage() instanceof MessageContext) {
			MessageContext requestContext = (MessageContext) e.getMessage();
			Object message = requestContext.getMessage();
			Object requestId = keyTransformer.transform(message);
			messageContextHolder.add(requestId, requestContext);
		}
		super.writeRequested(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		Object message = e.getMessage();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Received message: {}", message);
		}
		Object requestId = keyTransformer.transform(message);
		MessageContext requestContext = messageContextHolder.remove(requestId, message);
		if (requestContext != null) {
			processingTime.add(System.nanoTime() - requestContext.getTimestamp());
			requestContext.getClosure().execute(message);
		}
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("channel: [" + e.getChannel().getRemoteAddress() + "], caught exception in network layer", e.getCause());
		}
	}

	public void setKeyTransformer(KeyTransformer keyTransformer) {
		this.keyTransformer = keyTransformer;
	}

}
