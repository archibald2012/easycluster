package org.easycluster.easycluster.websocket.endpoint;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.endpoint.DefaultEndpoint;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketEndpoint extends DefaultEndpoint {

	private static final Logger			LOGGER		= LoggerFactory.getLogger(WebSocketEndpoint.class);

	private WebSocketServerHandshaker	handshaker	= null;

	public WebSocketEndpoint(Channel channel) {
		super(channel);
	}

	@Override
	public void send(final Object message) {
		if (message != null) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("send - [{}]", message);
			}
			ChannelFuture future = channel.write(new MessageContext(message));
			future.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (!future.isDone()) {
						if (null != future.getCause()) {
							LOGGER.error("Send message failed, message [" + message + "], channel: [" + future.getChannel().getRemoteAddress() + "], cause: ",
									future.getCause());
						} else {
							LOGGER.error("Send message failed without reason, message: [" + message + "], channel: [" + future.getChannel().getRemoteAddress()
									+ "]");
						}

					}
				}
			});
		}
	}

	public WebSocketServerHandshaker getHandshaker() {
		return handshaker;
	}

	public void setHandshaker(WebSocketServerHandshaker handshaker) {
		this.handshaker = handshaker;
	}

}
