//package org.easycluster.easycluster.cluster.netty.websocket;
//
//import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.CONTENT_TYPE;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
//import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.NOT_FOUND;
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.OK;
//import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
//
//import java.util.Collection;
//import java.util.Map;
//import java.util.TreeMap;
//
//import org.apache.zookeeper.server.SessionTracker.Session;
//import org.easycluster.easycluster.cluster.netty.session.SessionManager;
//import org.jboss.netty.buffer.ChannelBuffer;
//import org.jboss.netty.buffer.ChannelBuffers;
//import org.jboss.netty.channel.Channel;
//import org.jboss.netty.channel.ChannelFuture;
//import org.jboss.netty.channel.ChannelFutureListener;
//import org.jboss.netty.channel.ChannelHandlerContext;
//import org.jboss.netty.channel.ChannelStateEvent;
//import org.jboss.netty.channel.Channels;
//import org.jboss.netty.channel.ExceptionEvent;
//import org.jboss.netty.channel.MessageEvent;
//import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
//import org.jboss.netty.handler.codec.http.DefaultHttpResponse;
//import org.jboss.netty.handler.codec.http.HttpRequest;
//import org.jboss.netty.handler.codec.http.HttpResponse;
//import org.jboss.netty.handler.codec.http.websocketx.BinaryWebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.TextWebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
//import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
//import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
//import org.jboss.netty.util.CharsetUtil;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//public class InnerWebsocketHandler extends SimpleChannelUpstreamHandler {
//
//	private static final Logger	logger			= LoggerFactory.getLogger(InnerWebsocketHandler.class);
//
//	private static final String	WEBSOCKET_PATH	= "/ws";
//
//	private SessionManager		sessionManager;
//
//	@Override
//	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
//		Object msg = e.getMessage();
//		if (msg instanceof HttpRequest) {
//			handleHttpRequest(ctx, (HttpRequest) msg);
//		} else if (msg instanceof WebSocketFrame) {
//			handleWebSocketFrame(ctx, (WebSocketFrame) msg);
//		}
//	}
//
//	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest request) throws Exception {
//
//		if (request.getMethod() != GET) {
//			sendHttpResponse(ctx, request, new DefaultHttpResponse(HTTP_1_1, FORBIDDEN));
//			return;
//		}
//
//		if ("/".equals(request.getUri())) {
//			HttpResponse res = new DefaultHttpResponse(HTTP_1_1, OK);
//
//			ChannelBuffer content = WebSocketServerTestPage.getContent(getWebSocketLocation(request));
//
//			res.setHeader(CONTENT_TYPE, "text/html; charset=UTF-8");
//			setContentLength(res, content.readableBytes());
//
//			res.setContent(content);
//			sendHttpResponse(ctx, request, res);
//			return;
//		}
//		if ("/favicon.ico".equals(request.getUri())) {
//			HttpResponse res = new DefaultHttpResponse(HTTP_1_1, NOT_FOUND);
//			sendHttpResponse(ctx, request, res);
//			return;
//		}
//		Channel channel = ctx.getChannel();
//		// Handshake
//		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(request), null, false);
//
//		WebSocketServerHandshaker handshaker = wsFactory.newHandshaker(request);
//
//		if (handshaker == null) {
//			wsFactory.sendUnsupportedWebSocketVersionResponse(channel);
//		} else {
//			sessionManager.putChannel(channel.getId(), channel);
//			ctx.setAttachment(handshaker);
//			handshaker.handshake(ctx.getChannel(), request).addListener(new ChannelFutureListener() {
//
//				public void operationComplete(ChannelFuture future) throws Exception {
//					if (future.isSuccess()) {
//						Map<String, String> headers = new TreeMap<String, String>();
//						headers.put(HeaderUtils.CMD, HeaderUtils.TIME);
//						future.getChannel().write(new TextWebSocketFrame(HeaderUtils.buildHeader(headers).toString()));
//					} else {
//						Channels.fireExceptionCaught(future.getChannel(), future.getCause());
//					}
//				}
//			});
//		}
//	}
//
//	private void handleWebSocketFrame(ChannelHandlerContext ctx, WebSocketFrame frame) {
//		Channel channel = ctx.getChannel();
//		// Check for closing frame
//		if (frame instanceof CloseWebSocketFrame) {
//			WebSocketServerHandshaker handshaker = (WebSocketServerHandshaker) ctx.getAttachment();
//			if (handshaker != null) {
//				handshaker.close(channel, (CloseWebSocketFrame) frame);
//			} else {
//				channel.close();
//			}
//			return;
//		}
//		if (frame instanceof PingWebSocketFrame) {
//			ctx.getChannel().write(new PongWebSocketFrame());
//			return;
//		}
//		if (frame instanceof TextWebSocketFrame) {
//			TextWebSocketFrame textFrame = (TextWebSocketFrame) frame;
//			ChannelBuffer buffer = textFrame.getBinaryData();
//			Map<String, String> headers = HeaderUtils.parseHeader(buffer);
//			if (headers == null || headers.isEmpty()) {
//				if (logger.isErrorEnabled()) {
//					logger.error("can't parser headers.");
//				}
//				return;
//			}
//			String command = headers.get(HeaderUtils.CMD);
//			if (command == null || command.trim().equals("")) {
//				command = HeaderUtils.CMD_SEND;
//			}
//			if (HeaderUtils.CMD_SEND.equals(command)) {
//				String uid = headers.get(HeaderUtils.DEST);
//				String ack = headers.get(HeaderUtils.CMD_ACK);
//				String messageId = headers.get(HeaderUtils.SID);
//				if (HeaderUtils.ACK_1.equals(ack)) {
//					String newMessageId = channel.getId() + ":" + messageId;
//					headers.put(HeaderUtils.SID, newMessageId);
//				}
//				Collection<Session> sessions = sessionManager.getSession(uid);
//				if (sessions == null || sessions.isEmpty()) {
//					if (logger.isWarnEnabled()) {
//						logger.warn("not found session by  uid:" + uid);
//					}
//					return;
//				}
//				StringBuilder head = HeaderUtils.buildHeader(headers);
//				ChannelBuffer newBuffer = ChannelBuffers.dynamicBuffer();
//				newBuffer.writeBytes(head.toString().getBytes(HeaderUtils.UTF_8_CHARSET));
//				newBuffer.writeBytes(buffer, buffer.readerIndex(), buffer.readableBytes());
//				TextWebSocketFrame textWebSocketFrame = new TextWebSocketFrame(newBuffer);
//				if (logger.isInfoEnabled()) {
//					logger.info("inner write to outer: " + textWebSocketFrame.getText());
//				}
//				for (Session session : sessions) {
//					try {
//						ChannelFuture future = session.getChannel().write(textWebSocketFrame);
//						future.addListener(new ChannelFutureListener() {
//
//							public void operationComplete(ChannelFuture future) throws Exception {
//								if (!future.isSuccess()) {
//									if (logger.isWarnEnabled()) {
//										logger.warn("inner  fail to write  outer:" + future.getChannel() + ",the error:" + future.getCause());
//									}
//								}
//							}
//						});
//					} catch (Exception e) {
//						if (logger.isErrorEnabled()) {
//							logger.error("inner can't write to outer:" + textWebSocketFrame, e);
//						}
//					}
//				}
//				return;
//			}
//			if (HeaderUtils.CMD_ACK.equals(command)) {
//
//				return;
//			}
//			return;
//		}
//
//		if (frame instanceof BinaryWebSocketFrame) {
//			// BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
//			// System.out.println("frame.isDirect:"
//			// + binaryFrame.getBinaryData().isDirect());
//			// BinaryWebSocketFrame binaryFrame = (BinaryWebSocketFrame) frame;
//			// ChannelBuffer binaryData = binaryFrame.getBinaryData();
//			// binaryData.getByte(0);
//			// binaryFrame.getRsv()
//			return;
//		}
//		throw new UnsupportedOperationException(String.format("%s frame types not supported", frame.getClass().getName()));
//
//	}
//
//	private static void sendHttpResponse(ChannelHandlerContext ctx, HttpRequest req, HttpResponse res) {
//		if (res.getStatus().getCode() != 200) {
//			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus().toString(), CharsetUtil.UTF_8));
//			setContentLength(res, res.getContent().readableBytes());
//		}
//		ChannelFuture f = ctx.getChannel().write(res);
//		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
//			f.addListener(ChannelFutureListener.CLOSE);
//		}
//	}
//
//	@Override
//	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e) throws Exception {
//		if (logger.isErrorEnabled()) {
//			logger.error("inner exceptionCaught:", e.getCause());
//		}
//		Channel channel = e.getChannel();
//		sessionManager.removeChannel(channel.getId());
//		channel.close();
//
//	}
//
//	public void channelConnected(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
//		if (logger.isInfoEnabled()) {
//			logger.info("the inner channel [" + ctx.getChannel() + "]  is connected.");
//		}
//	}
//
//	@Override
//	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent e) throws Exception {
//		if (logger.isWarnEnabled()) {
//			logger.warn(" the inner channel[" + ctx.getChannel() + "] was closed.");
//		}
//		sessionManager.removeChannel(ctx.getChannel().getId());
//	}
//
//	private static String getWebSocketLocation(HttpRequest req) {
//		return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
//	}
//
//	public SessionManager getSessionManager() {
//		return sessionManager;
//	}
//
//	public void setSessionManager(SessionManager sessionManager) {
//		this.sessionManager = sessionManager;
//	}
//
//}
