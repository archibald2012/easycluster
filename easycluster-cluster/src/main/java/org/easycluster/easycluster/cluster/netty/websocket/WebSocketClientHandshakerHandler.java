package org.easycluster.easycluster.cluster.netty.websocket;

import java.net.URI;
import java.util.HashMap;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientHandshakerHandler extends SimpleChannelUpstreamHandler {

	private static final Logger					LOGGER		= LoggerFactory.getLogger(WebSocketClientHandshakerHandler.class);

	private volatile WebSocketClientHandshaker	handshaker	= null;

	public WebSocketClientHandshakerHandler(URI uri) {
		// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08 or
		// V00.
		// If you change it to V00, ping is not supported and remember to change
		// HttpResponseDecoder to WebSocketHttpResponseDecoder in the pipeline.
		handshaker = new WebSocketClientHandshakerFactory().newHandshaker(uri, WebSocketVersion.V13, null, false, new HashMap<String, String>());

	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("channelConnected: " + e.getChannel());
		}

		handshaker.handshake(e.getChannel()).syncUninterruptibly();
	}

	@Override
	public void channelDisconnected(ChannelHandlerContext ctx, final ChannelStateEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("channelDisconnected: " + e.getChannel());
		}
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Channel ch = ctx.getChannel();
		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ch, (HttpResponse) e.getMessage());
			LOGGER.info("Handshake completed, webSocket Client connected!", e.getChannel());
			return;
		}

		if (e.getMessage() instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) e.getMessage();
			throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content=" + response.getContent().toString(CharsetUtil.UTF_8)
					+ ")");
		}

		WebSocketFrame frame = (WebSocketFrame) e.getMessage();
		if (frame instanceof PongWebSocketFrame) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("WebSocket Client received pong, [{}]", frame);
			}
		} else if (frame instanceof CloseWebSocketFrame) {
			ch.close();
		}

	}

}
