package org.easycluster.easycluster.websocket;

import java.net.InetSocketAddress;
import java.net.SocketAddress;
import java.net.URI;
import java.util.HashMap;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.netty.MessageContextHolder;
import org.easycluster.easycluster.core.KeyTransformer;
import org.easycluster.easycluster.core.Transformer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientChannelHandler extends SimpleChannelHandler {

	private static final Logger					LOGGER					= LoggerFactory.getLogger(WebSocketClientChannelHandler.class);

	private MessageContextHolder				messageContextHolder	= null;
	private KeyTransformer						keyTransformer			= new KeyTransformer();
	private WebSocketClientHandshaker			handshaker				= null;
	private Transformer<WebSocketFrame, Object>	webSocketFrameDecoder	= null;
	private Transformer<Object, WebSocketFrame>	webSocketFrameEncoder	= null;

	public WebSocketClientChannelHandler(MessageContextHolder messageContextHolder) {
		this.messageContextHolder = messageContextHolder;
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			ChannelFuture handshakeFuture = sslHandler.handshake();
			handshakeFuture.addListener(new ChannelFutureListener() {

				@Override
				public void operationComplete(ChannelFuture future) throws Exception {
					if (future.isSuccess()) {
						if (LOGGER.isDebugEnabled()) {
							LOGGER.debug("Your session is protected by " + sslHandler.getEngine().getSession().getCipherSuite() + " cipher suite.\n");
						}
					} else {
						future.getChannel().close();
					}
				}
			});
		}

		Channel channel = ctx.getChannel();

		SocketAddress address = channel.getRemoteAddress();
		if (address instanceof InetSocketAddress) {
			InetSocketAddress inetAddresses = (InetSocketAddress) address;
			String host = inetAddresses.getAddress().getHostAddress();

			URI uri = new URI("ws://" + host + ":" + 6000 + "/ws");

			// Connect with V13 (RFC 6455 aka HyBi-17). You can change it to V08
			// or
			// V00.
			// If you change it to V00, ping is not supported and remember to
			// change
			// HttpResponseDecoder to WebSocketHttpResponseDecoder in the
			// pipeline.
			final WebSocketClientHandshaker handshaker = new WebSocketClientHandshakerFactory().newHandshaker(uri, WebSocketVersion.V13, null, false,
					new HashMap<String, String>());

			handshaker.handshake(channel).syncUninterruptibly();

			LOGGER.info("Handshake completed, webSocket Client connected!", e.getChannel());
		}

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
			WebSocketFrame request = webSocketFrameEncoder.transform(message);
			Channels.write(ctx, e.getFuture(), request);
		}
		super.writeRequested(ctx, e);
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("messageReceived: " + e.getMessage());
		}

		if (!handshaker.isHandshakeComplete()) {
			handshaker.finishHandshake(ctx.getChannel(), (HttpResponse) e.getMessage());
			LOGGER.info("Handshake completed, webSocket Client connected!", e.getChannel());
			return;
		}

		if (e.getMessage() instanceof HttpResponse) {
			HttpResponse response = (HttpResponse) e.getMessage();
			throw new Exception("Unexpected HttpResponse (status=" + response.getStatus() + ", content=" + response.getContent().toString(CharsetUtil.UTF_8)
					+ ")");
		}

		WebSocketFrame frame = (WebSocketFrame) e.getMessage();
		if (frame instanceof BinaryWebSocketFrame) {
			Object message = webSocketFrameDecoder.transform((BinaryWebSocketFrame) frame);
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Received message: {}", message);
			}
			Object requestId = keyTransformer.transform(message);
			MessageContext requestContext = messageContextHolder.remove(requestId, message);
			if (requestContext != null) {
				requestContext.getClosure().execute(message);
			}
		} else if (frame instanceof TextWebSocketFrame) {

		} else if (frame instanceof PingWebSocketFrame) {
			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Received ping, channel:" + ctx.getChannel());
			}
			ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
		} else if (frame instanceof PongWebSocketFrame) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("WebSocket Client received pong, [{}]", frame);
			}
		} else if (frame instanceof CloseWebSocketFrame) {
			ctx.getChannel().close();
		} else {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
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

	public void setWebSocketFrameDecoder(Transformer<WebSocketFrame, Object> webSocketFrameDecoder) {
		this.webSocketFrameDecoder = webSocketFrameDecoder;
	}

	public void setWebSocketFrameEncoder(Transformer<Object, WebSocketFrame> webSocketFrameEncoder) {
		this.webSocketFrameEncoder = webSocketFrameEncoder;
	}

}
