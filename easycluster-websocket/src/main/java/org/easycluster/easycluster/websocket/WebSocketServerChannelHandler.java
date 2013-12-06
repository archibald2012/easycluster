package org.easycluster.easycluster.websocket;

import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONNECTION;
import static org.jboss.netty.handler.codec.http.HttpHeaders.Values.WEBSOCKET;

import java.io.UnsupportedEncodingException;
import java.net.InetSocketAddress;
import java.net.SocketAddress;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.endpoint.DefaultEndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.Endpoint;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointListener;
import org.easycluster.easycluster.cluster.security.BlackList;
import org.easycluster.easycluster.cluster.server.MessageClosureRegistry;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
import org.easycluster.easycluster.core.Closure;
import org.easycluster.easycluster.core.Identifiable;
import org.easycluster.easycluster.core.KeyTransformer;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.core.TransportUtil;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelFuture;
import org.jboss.netty.channel.ChannelFutureListener;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.Channels;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpHeaders.Names;
import org.jboss.netty.handler.codec.http.HttpHeaders.Values;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.jboss.netty.handler.ssl.SslHandler;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerChannelHandler extends IdleStateAwareChannelUpstreamHandler {

	private static final Logger					LOGGER					= LoggerFactory.getLogger(WebSocketServerChannelHandler.class);

	private static final String					WEBSOCKET_PATH			= "/ws";

	private ChannelGroup						channelGroup			= null;
	private MessageClosureRegistry				messageHandlerRegistry	= null;
	private MessageExecutor						messageExecutor			= null;
	private KeyTransformer						keyTransformer			= new KeyTransformer();
	private EndpointFactory						endpointFactory			= new DefaultEndpointFactory();
	private final ChannelLocal<Endpoint>		endpoints				= new ChannelLocal<Endpoint>();
	private volatile WebSocketServerHandshaker	handshaker				= null;
	private BlackList							blackList				= null;
	private Transformer<WebSocketFrame, Object>	webSocketFrameDecoder	= null;
	private Transformer<Object, WebSocketFrame>	webSocketFrameEncoder	= null;

	public WebSocketServerChannelHandler(final ChannelGroup channelGroup, final MessageClosureRegistry messageHandlerRegistry,
			final MessageExecutor messageExecutor, final BlackList blackList) {
		this.channelGroup = channelGroup;
		this.messageHandlerRegistry = messageHandlerRegistry;
		this.messageExecutor = messageExecutor;
		this.blackList = blackList;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("channelOpen: " + channel);
		}
		// the following should be done really fast because it runs under a boss
		// thread
		if (blackList != null) {
			SocketAddress address = channel.getRemoteAddress();
			if (address instanceof InetSocketAddress) {
				InetSocketAddress inetAddresses = (InetSocketAddress) address;
				String hostAddress = inetAddresses.getAddress().getHostAddress();
				if (blackList.containsIp(hostAddress)) {
					if (LOGGER.isInfoEnabled()) {
						LOGGER.info("Close channel now since the ip '{}' is in the blacklist {}.", hostAddress, blackList);
					}
					channel.close();
					return;
				}
			}
		}
		Endpoint endpoint = endpointFactory.createEndpoint(e.getChannel());
		if (null != endpoint) {
			attachEndpointToSession(e.getChannel(), endpoint);
		}
		channelGroup.add(channel);
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		final SslHandler sslHandler = ctx.getPipeline().get(SslHandler.class);
		if (sslHandler != null) {
			// Get notified when SSL handshake is done.
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
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
		LOGGER.error("channel: [" + e.getChannel().getRemoteAddress() + "], exceptionCaught:", e.getCause());
		// ctx.getChannel().close();
	}

	@Override
	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("channelClosed: [" + e.getChannel().getRemoteAddress() + "]");
		}
		Endpoint endpoint = removeEndpointOfSession(e.getChannel());
		if (null != endpoint) {
			endpoint.stop();
		}
	}

	@Override
	public void channelIdle(ChannelHandlerContext ctx, IdleStateEvent e) throws Exception {
		if (LOGGER.isInfoEnabled()) {
			LOGGER.info("channelIdle: " + e.getState().name() + " for " + (System.currentTimeMillis() - e.getLastActivityTimeMillis())
					+ " milliseconds, close channel[" + e.getChannel().getRemoteAddress() + "]");
		}
		e.getChannel().close();
	}

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof WebSocketFrame) {
			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
		}
	}

	private void handleHttpRequest(final ChannelHandlerContext ctx, final HttpRequest req) throws Exception {

		Channel channel = ctx.getChannel();

		// Allow only GET methods.
		if (req.getMethod() != HttpMethod.GET) {
			sendResponse(channel, HttpResponseStatus.FORBIDDEN);
			return;
		}

		// Send the demo page and favicon.ico
		if (req.getUri().equals("/")) {
			sendResponse(channel, HttpResponseStatus.OK, WebSocketServerIndexPage.getContent(getWebSocketLocation(req)), "UTF-8", "text/html; charset=UTF-8");
			return;
		} else if (req.getUri().equals("/favicon.ico")) {
			sendResponse(channel, HttpResponseStatus.NOT_FOUND);
			return;
		}

		if (Values.UPGRADE.equalsIgnoreCase(req.getHeader(CONNECTION)) && WEBSOCKET.equalsIgnoreCase(req.getHeader(Names.UPGRADE))) {
			WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
			final WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(req);

			// Handshake
			if (handshaker == null) {
				wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
			} else {
				ChannelFuture future = handshaker.handshake(ctx.getChannel(), req);
				future.addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
				future.addListener(new ChannelFutureListener() {
					@Override
					public void operationComplete(ChannelFuture future) throws Exception {
						if (!future.isSuccess()) {
							Channels.fireExceptionCaught(future.getChannel(), future.getCause());
						}
					}
				});
			}
		}
	}

	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {

		Endpoint endpoint = (Endpoint) ctx.getChannel().getAttachment();
		if (null == endpoint) {
			LOGGER.warn("missing endpoint, ignore incoming msg:", frame);
			return;
		}

		Object signal = null;

		if (frame instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) frame);
			return;
		} else if (frame instanceof PingWebSocketFrame) {
			ctx.getChannel().write(new PongWebSocketFrame(frame.getBinaryData()));
			return;
		} else if (frame instanceof TextWebSocketFrame) {
			signal = webSocketFrameDecoder.transform(frame);
		} else if (frame instanceof BinaryWebSocketFrame) {
			signal = webSocketFrameDecoder.transform(frame);
		} else {
			throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
		}

		if (signal != null) {
			TransportUtil.attachSender(signal, endpoint);
			ResponseHandler responseHandler = new ResponseHandler(ctx.getChannel(), signal);

			if (!messageHandlerRegistry.messageRegistered(signal.getClass())) {
				String error = String.format("No such message of type %s registered", signal.getClass().getName());
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(error);
				}
				responseHandler.execute(new InvalidMessageException(error));
			} else {
				messageExecutor.execute(signal, responseHandler);
			}
		}
	}

	class ResponseHandler implements Closure {
		private Channel	channel;
		private Object	request;

		public ResponseHandler(Channel channel, Object request) {
			this.channel = channel;
			this.request = request;
		}

		@Override
		public void execute(Object message) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Reply message: {}", ToStringBuilder.reflectionToString(message, ToStringStyle.SHORT_PREFIX_STYLE));
			}

			Exception ex = null;

			if (message instanceof Exception) {
				ex = (Exception) message;
				message = buildErrorResponse(ex);
			}

			if (message instanceof Identifiable) {
				Object requestId = keyTransformer.transform(request);
				((Identifiable) message).setIdentification((Long) requestId);
			}

			WebSocketFrame frame = webSocketFrameEncoder.transform(message);

			channel.write(frame);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Sent response: {}", frame);
			}
		}

		private Object buildErrorResponse(Exception ex) {
			Class<?> responseType = messageHandlerRegistry.getResponseTypeFor(request);
			if (responseType == null) {
				return null;
			}

			Object response = null;
			try {
				response = responseType.newInstance();
				// TODO set exception message
			} catch (Exception e) {
				LOGGER.error("Build default response with error " + e.getMessage(), e);
			}
			return response;
		}
	}

	public void attachEndpointToSession(Channel channel, Endpoint endpoint) {
		endpoints.set(channel, endpoint);
	}

	public Endpoint getEndpointOfSession(Channel channel) {
		return (Endpoint) endpoints.get(channel);
	}

	public Endpoint removeEndpointOfSession(Channel channel) {
		return (Endpoint) endpoints.remove(channel);
	}

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	}

	private void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus) {
		sendResponse(channel, httpResponseStatus, "", CharsetUtil.UTF_8.name(), null);
	}

	private void sendResponse(Channel channel, HttpResponseStatus httpResponseStatus, String responseContent, String charsetName, String contentType) {
		try {
			HttpResponse response = new DefaultHttpResponse(HttpVersion.HTTP_1_1, httpResponseStatus);
			byte[] contents = responseContent.getBytes(charsetName);
			response.setContent(ChannelBuffers.wrappedBuffer(contents));
			response.setHeader(HttpHeaders.Names.CONTENT_LENGTH, response.getContent().readableBytes());
			if (contentType != null) {
				response.setHeader(HttpHeaders.Names.CONTENT_TYPE, contentType);
			}
			ChannelFuture future = channel.write(response);
			if (!HttpHeaders.isKeepAlive(response) || response.getStatus() != HttpResponseStatus.OK) {
				// no content
				future.addListener(ChannelFutureListener.CLOSE);
			}
		} catch (UnsupportedEncodingException ignore) {
			ignore.printStackTrace();
		}
	}

	public void setEndpointFactory(EndpointFactory endpointFactory) {
		this.endpointFactory = endpointFactory;
	}

	public void setEndpointListener(EndpointListener endpointListener) {
		this.endpointFactory.setEndpointListener(endpointListener);
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
