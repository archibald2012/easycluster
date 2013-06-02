package org.easycluster.easycluster.cluster.netty.websocket;

import java.net.InetSocketAddress;
import java.net.URI;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.common.AverageTimeTracker;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.common.RequestsPerSecondTracker;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.codec.ByteBeanDecoder;
import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketClientHandshakerFactory;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketVersion;
import org.jboss.netty.util.CharsetUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketClientHandler extends SimpleChannelHandler {

	private final Logger								LOGGER			= LoggerFactory.getLogger(WebSocketClientHandler.class);

	private ConcurrentHashMap<Object, MessageContext>	requestMap		= new ConcurrentHashMap<Object, MessageContext>();
	private MessageRegistry								messageRegistry;

	private AverageTimeTracker							processingTime	= new AverageTimeTracker(100);
	private RequestsPerSecondTracker					rps				= new RequestsPerSecondTracker();
	private KeyTransformer								keyTransformer	= new KeyTransformer();
	private ByteBeanDecoder								byteBeanDecoder	= new ByteBeanDecoder();
	private volatile WebSocketClientHandshaker			handshaker		= null;

	public WebSocketClientHandler(MessageRegistry messageRegistry, final int staleRequestTimeoutMins, final int staleRequestCleanupFrequencyMins) {
		this.messageRegistry = messageRegistry;

		Thread cleanupThread = new Thread("stale-request-cleanup-thread") {
			long	staleRequestTimeoutMillis	= TimeUnit.MILLISECONDS.convert(staleRequestTimeoutMins, TimeUnit.MINUTES);

			public void run() {
				while (true) {
					try {
						TimeUnit.MINUTES.sleep(staleRequestCleanupFrequencyMins);
					} catch (InterruptedException e) {
						continue;
					}

					for (Object key : requestMap.keySet()) {
						MessageContext request = requestMap.get(key);
						if ((System.currentTimeMillis() - request.getTimestamp()) > staleRequestTimeoutMillis) {
							requestMap.remove(key);
						}
					}

				}
			}

		};

		cleanupThread.setDaemon(true);
		cleanupThread.start();
	}

	@Override
	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("channelConnected: " + e.getChannel());
		}

		InetSocketAddress address = (InetSocketAddress) e.getChannel().getRemoteAddress();
		URI webSocketURL = new URI("ws://" + address.getHostName() + ":" + address.getPort());

		handshaker = new WebSocketClientHandshakerFactory().newHandshaker(webSocketURL, WebSocketVersion.V13, null, false, new HashMap<String, String>());

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
		if (frame instanceof BinaryWebSocketFrame) {

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("receive websocket frame: [{}]", frame);
			}

			ChannelBuffer content = ((BinaryWebSocketFrame) frame).getBinaryData();
			if (null != content) {
				Object message = byteBeanDecoder.transform(content);

				Object requestId = keyTransformer.transform(message);
				MessageContext requestContext = requestMap.get(requestId);
				if (requestContext == null) {
					LOGGER.warn("Received a response message [%s] without a corresponding request", message);
				} else {
					requestMap.remove(requestId);

					Class<? extends Object> requestType = requestContext.getMessage().getClass();

					if (messageRegistry.validResponseFor(requestType, message)) {
						requestContext.getClosure().execute(message);
					} else {
						throw new InvalidMessageException(String.format("Response message of type %s doesn't match registered response for %s", message
								.getClass().getName(), requestType.getName()));
					}

				}
			}

		} else if (frame instanceof PongWebSocketFrame) {
			if (LOGGER.isTraceEnabled()) {
				LOGGER.trace("Receive websocket pong frame, [{}]", frame);
			}
		} else if (frame instanceof CloseWebSocketFrame) {
			ch.close();
		}

	}

	@Override
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e) throws Exception {

		if (e.getMessage() instanceof MessageContext) {
			MessageContext requestContext = (MessageContext) e.getMessage();
			Object request = requestContext.getMessage();
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Writing request: {}", request);
			}

			if (messageRegistry.hasResponse(request.getClass())) {
				Object requestId = keyTransformer.transform(request);
				requestMap.put(requestId, requestContext);
			}
		}

		super.writeRequested(ctx, e);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) {
		LOGGER.info("Caught exception in network layer", e.getCause());
	}

	public void setKeyTransformer(KeyTransformer keyTransformer) {
		this.keyTransformer = keyTransformer;
	}

	class NetworkClientStatisticsMBean {
		public int getRequestsPerSecond() {
			return rps.get();
		}

		public long getAverageRequestProcessingTime() {
			return processingTime.average();
		}
	}

}
