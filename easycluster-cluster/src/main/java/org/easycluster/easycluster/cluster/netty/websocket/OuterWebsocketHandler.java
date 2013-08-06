//package org.easycluster.easycluster.cluster.netty.websocket;
//
//import static org.jboss.netty.handler.codec.http.HttpHeaders.isKeepAlive;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.setContentLength;
//import static org.jboss.netty.handler.codec.http.HttpHeaders.Names.HOST;
//import static org.jboss.netty.handler.codec.http.HttpMethod.GET;
//import static org.jboss.netty.handler.codec.http.HttpResponseStatus.FORBIDDEN;
//import static org.jboss.netty.handler.codec.http.HttpVersion.HTTP_1_1;
//
//import java.net.InetSocketAddress;
//import java.net.SocketAddress;
//import java.util.HashMap;
//import java.util.Map;
//import java.util.TreeMap;
//
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
//import com.tmall.wireless.platform.common.HeaderUtils;
//import com.tmall.wireless.platform.common.caches.CacheCallback;
//import com.tmall.wireless.platform.common.caches.CacheService;
//import com.tmall.wireless.platform.common.status.Statistics;
//import com.tmall.wireless.platform.common.utils.StringUtils;
//import com.tmall.wireless.platform.connector.api.ApiContext;
//import com.tmall.wireless.platform.connector.api.ApiHandler;
//import com.tmall.wireless.platform.connector.service.AuthService;
//import com.tmall.wireless.platform.connector.service.AuthService.AuthCallback;
//import com.tmall.wireless.platform.connector.service.BlacklistService;
//import com.tmall.wireless.platform.connector.session.Session;
//import com.tmall.wireless.platform.connector.session.SessionManager;
//import com.tmall.wireless.platform.connector.user.User;
//
//public class OuterWebsocketHandler extends SimpleChannelUpstreamHandler {
//
//	private static Logger logger = LoggerFactory
//			.getLogger(OuterWebsocketHandler.class);
//
//	private static final String WEBSOCKET_PATH = "/ws";
//
//	private static final Object DUPLICATED_CONNECT = new Object();
//
//	private SessionManager sessionManager;
//
//	private CacheService cacheService;
//
//	private ServerConfiguration configuration;
//
//	private String innerLocalAddress;
//
//	private String innerLocalAddressAndTime;
//
//	private AuthService authService;
//
//	private BlacklistService blacklistService;
//
//	public void init() {
//		this.innerLocalAddress = getInnerLocalAddress();
//		this.innerLocalAddressAndTime = getInnerLocalAddress() + ":"
//				+ configuration.getStartTime();
//	}
//
//	public void destroy() {
//
//	}
//
//	@Override
//	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e)
//			throws Exception {
//		Object msg = e.getMessage();
//		if (msg instanceof HttpRequest) {
//			handleHttpRequest(ctx, e, (HttpRequest) msg);
//		} else if (msg instanceof WebSocketFrame) {
//			handleWebSocketFrame(ctx, e, (WebSocketFrame) msg);
//		}
//	}
//
//	private void handleHttpRequest(final ChannelHandlerContext ctx,
//			MessageEvent e, final HttpRequest request) throws Exception {
//		final Channel channel = ctx.getChannel();
//		if (logger.isInfoEnabled()) {
//			logger.info("coming request:" + request + " , the channel:"
//					+ channel);
//		}
//		if (request.getMethod() != GET) {
//			sendHttpResponse(ctx, request, new DefaultHttpResponse(HTTP_1_1,
//					FORBIDDEN));
//			return;
//		}
//		// Handshake
//		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(
//				getWebSocketLocation(request), null, false);
//		final WebSocketServerHandshaker handshaker = wsFactory
//				.newHandshaker(request);
//		if (handshaker == null) {
//			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
//		} else {
//			Statistics.increment(Statistics.CON_USER_LOGIN);
//			ChannelFuture future = handshaker.handshake(channel, request);
//			future.addListener(new ChannelFutureListener() {
//				@Override
//				public void operationComplete(ChannelFuture future)
//						throws Exception {
//					if (!future.isSuccess()) {
//						Channels.fireExceptionCaught(future.getChannel(),
//								future.getCause());
//						return;
//					}
//					authService.authenticateHttpRequest(request,
//							new AuthService.AuthCallback() {
//
//								public void callback(final User user,
//										Exception e) {
//									if (e != null) {
//										notifyFailLoginToOuter(channel, e, user);
//										if (logger.isErrorEnabled()) {
//											logger.error("the authenticate fail ,the user:"
//													+ user + ",the error: " + e);
//										}
//										return;
//									}
//									Session session = new Session(user,
//											channel, handshaker);
//									ctx.setAttachment(session);
//									addSession(channel, session, user);
//								}
//							});
//				}
//			});
//
//		}
//	}
//
//	private void addSession(final Channel channel, Session session, User user) {
//		Session oldSession = sessionManager.putSession(session);
//		if (oldSession != null) {
//			oldSession.getChannel().setAttachment(DUPLICATED_CONNECT);
//			oldSession.getHandshaker().close(oldSession.getChannel(),
//					new CloseWebSocketFrame(1000, "duplicated  connected!"));
//			if (logger.isInfoEnabled()) {
//				logger.info("the uid:" + session.getUserId()
//						+ ",reduplicate connecting ,two channels:\r\n"
//						+ channel + "\r\n" + oldSession.getChannel());
//			}
//		}
//		notifyConnectState(user, channel);
//	}
//
//	private void notifyConnectState(final User user, final Channel channel) {
//		try {
//			cacheService.sAddAsync(user.getId(), this.innerLocalAddressAndTime,
//					0, new CacheCallback() {
//						public void callback(boolean success, int code,
//								String message) {
//							if (code != -20004 && !success) {
//								notifyFailLoginToOuter(channel,
//										new RuntimeException(
//												"fail  to invoke tair!"), user);
//							} else {
//								notifyLoginEventToOuter(user, channel);
//							}
//						}
//					});
//		} catch (Exception e) {
//			notifyFailLoginToOuter(channel, new RuntimeException(
//					"fail  to invoke tair!", e), user);
//			if (logger.isErrorEnabled()) {
//				logger.error("can't synchronize user:" + user.getId()
//						+ " connect state to tair!,the error:" + e);
//			}
//		}
//		notifyLoginEventToRouter(user, channel);
//	}
//
//	private void notifyLoginEventToRouter(final User user, Channel channel) {
//
//		Map<String, String> headers = new TreeMap<String, String>();
//		headers.put(HeaderUtils.CMD, HeaderUtils.CMD_AUTH);
//		headers.put(HeaderUtils.DEST, user.getId());
//		headers.put(HeaderUtils.AT, this.innerLocalAddress);
//
//		final TextWebSocketFrame loginMessage = new TextWebSocketFrame(
//				HeaderUtils.buildHeader(headers).toString());
//
//		Channel innerChannel = sessionManager.randomChannel();
//		if (innerChannel == null) {
//			if (logger.isErrorEnabled()) {
//				logger.error("can't notify login message,the uid: "
//						+ user.getId() + ",not found inner channel!");
//			}
//			return;
//		}
//		if (logger.isInfoEnabled()) {
//			logger.info("outer:" + channel + " write to inner:" + innerChannel
//					+ ",text:" + loginMessage.getText());
//		}
//		ChannelFuture future = innerChannel.write(loginMessage);
//
//		future.addListener(new ChannelFutureListener() {
//
//			public void operationComplete(ChannelFuture future)
//					throws Exception {
//				if (!future.isSuccess()) {
//					// retry
//					Channel innerChannel = sessionManager.randomChannel();
//					if (innerChannel == null) {
//						if (logger.isErrorEnabled()) {
//							logger.error("can't sencond notify login message,the uid: "
//									+ user.getId()
//									+ ",not found inner channel!");
//						}
//						return;
//					}
//					ChannelFuture sencondFuture = innerChannel
//							.write(loginMessage);
//					sencondFuture.addListener(new ChannelFutureListener() {
//
//						public void operationComplete(ChannelFuture future)
//								throws Exception {
//							if (logger.isErrorEnabled()) {
//								logger.error("can't notify login message,the uid: "
//										+ user.getId());
//							}
//						}
//					});
//				}
//			}
//		});
//	}
//
//	private void notifyLoginEventToOuter(final User user, Channel channel) {
//		Map<String, String> headers = new TreeMap<String, String>();
//		headers.put(HeaderUtils.CMD, HeaderUtils.CMD_AUTH);
//		headers.put(HeaderUtils.CODE, "0");
//		headers.put(HeaderUtils.HEARTBEAT_INTERVAL,
//				String.valueOf(user.getHeartbeatInterval() / 1000));
//		if (user.isNewSn()) {
//			headers.put(HeaderUtils.SN, user.getSn());
//		}
//		// headers.put(HeaderUtils.CODE, "0");
//		final TextWebSocketFrame loginMessage = new TextWebSocketFrame(
//				HeaderUtils.buildHeader(headers).toString());
//		if (logger.isInfoEnabled()) {
//			logger.info("write to outter[" + channel + "], loginMessage:"
//					+ loginMessage.getText());
//		}
//		ChannelFuture future = channel.write(loginMessage);
//		future.addListener(new ChannelFutureListener() {
//
//			public void operationComplete(ChannelFuture future)
//					throws Exception {
//				if (!future.isSuccess()) {
//					if (logger.isErrorEnabled()) {
//						logger.error("can't write login event for the user:"
//								+ user.getId() + "");
//					}
//				}
//			}
//		});
//	}
//
//	private void notifyFailLoginToOuter(Channel channel, Exception e, User user) {
//		Map<String, String> headers = new HashMap<String, String>();
//		headers.put(HeaderUtils.CMD, HeaderUtils.CMD_AUTH);
//		headers.put(HeaderUtils.CODE, "1");
//		TextWebSocketFrame loginMessage = new TextWebSocketFrame(HeaderUtils
//				.buildHeader(headers).toString() + "{msg:'fail to login'}");
//		if (logger.isWarnEnabled()) {
//			logger.info("write to outter[" + channel + "], failLoginMessage:"
//					+ loginMessage.getText());
//		}
//		ChannelFuture future = channel.write(loginMessage);
//		future.addListener(ChannelFutureListener.CLOSE);
//	}
//
//	private String getInnerLocalAddress() {
//		return configuration.getInnerInetSocketAddress().getAddress()
//				.getHostAddress()
//				+ ":" + configuration.getInnerInetSocketAddress().getPort();
//	}
//
//	private void handleWebSocketFrame(final ChannelHandlerContext ctx,
//			MessageEvent e, WebSocketFrame frame) throws Exception {
//		final Channel channel = ctx.getChannel();
//		Object object = ctx.getAttachment();
//		Session session = null;
//		if (object != null && (object instanceof Session)) {
//			session = (Session) object;
//		}
//		if (session == null) {
//			channel.close();
//			return;
//		}
//		session.updateLastLiveTime();
//		if (frame instanceof TextWebSocketFrame) {
//			Statistics.increment(Statistics.CON_OUT_MSG);
//			if (logger.isInfoEnabled()) {
//				String text = ((TextWebSocketFrame) frame).getText();
//				logger.info("outer[" + channel + "] receive frame:" + text);
//			}
//			ChannelBuffer buffer = frame.getBinaryData();
//			Map<String, String> headers = HeaderUtils.parseHeader(buffer);
//			if (headers == null || headers.isEmpty()) {
//				if (logger.isErrorEnabled()) {
//					String text = ((TextWebSocketFrame) frame).getText();
//					logger.error("can't parser header,the frame:" + text);
//				}
//				return;
//			}
//			String type = headers.get(HeaderUtils.TYPE);
//			String command = headers.get(HeaderUtils.CMD);
//			if (command == null || command.trim().equals("")) {
//				command = HeaderUtils.CMD_SEND;
//			}
//			if (HeaderUtils.CMD_INVOKE.equals(command)
//					|| HeaderUtils.TYPE_API.equals(type)) {
//				String uid = session.getUserId();
//				if (StringUtils.isNotEmpty(uid) && StringUtils.isNumeric(uid)) {
//					
//				} else {
//					uid = "";
//				}
//				String id = headers.get("id");
//				String topic = headers.get("topic");
//				String body = HeaderUtils.readBody(buffer).trim();
//				if (body.startsWith("{")) {
//					body = "{\"uid\":\"" + uid + "\"," + body.substring(1);
//				}
//				ApiHandler.invoke(new ApiContext(channel, id, topic, body));
//				return;
//			}
//			if (HeaderUtils.CMD_ACK.equals(command)) {
//				Statistics.increment(Statistics.CON_MSG_ACK);
//				String sid = headers.get(HeaderUtils.SID);
//				if (sid == null || !sid.contains(":")) {
//					if (logger.isErrorEnabled()) {
//						logger.error("invalid sid:" + sid);
//					}
//					return;
//				}
//				int splitIndex = sid.indexOf(':');
//				String channelId = sid.substring(0, splitIndex);
//				Channel innerChannel = sessionManager.getChannel(Integer
//						.valueOf(channelId));
//				if (innerChannel == null) {
//					if (logger.isErrorEnabled()) {
//						logger.error("not found channel' ID" + channelId);
//					}
//					return;
//				}
//				headers.put(HeaderUtils.SID, sid.substring(splitIndex + 1));
//				headers.put(HeaderUtils.DEST, session.getUserId());
//				String head = HeaderUtils.buildHeader(headers).toString();
//				ChannelBuffer newBuffer = ChannelBuffers.dynamicBuffer();
//				newBuffer.writeBytes(head.getBytes(HeaderUtils.UTF_8_CHARSET));
//				newBuffer.writeBytes(buffer, buffer.readerIndex(),
//						buffer.readableBytes());
//				ChannelFuture channelFuture = innerChannel
//						.write(new TextWebSocketFrame(newBuffer));
//				channelFuture.addListener(new ChannelFutureListener() {
//
//					public void operationComplete(ChannelFuture future)
//							throws Exception {
//						if (!future.isSuccess()) {
//							if (logger.isErrorEnabled()) {
//								logger.error("fail to write to ["
//										+ future.getChannel() + "],the error:"
//										+ future.getCause());
//							}
//						}
//					}
//				});
//				return;
//			}
//			if (HeaderUtils.CMD_SEND.equals(command)) {
//				Statistics.increment(Statistics.CON_MSG_RECEIVE);
//				return;
//			}
//			if (HeaderUtils.CMD_AUTH.equals(command)) {
//				// 已经连接了，切换认证状态
//				String op = headers.get("op");
//				final Session oldSession = session;
//				if ("login".equals(op)) {
//					authService.authenticateFrame(headers, oldSession,
//							new AuthCallback() {
//
//								public void callback(User user, Exception e) {
//									if (e != null) {
//										// notify client
//										notifyFailLoginToOuter(channel, e, user);
//										if (logger.isErrorEnabled()) {
//											logger.error("the authenticate fail ,the user:"
//													+ user + ",the error: " + e);
//										}
//										return;
//									}
//									Session newSession = new Session(user,
//											channel, oldSession.getHandshaker());
//									ctx.setAttachment(newSession);
//									addSession(channel, newSession, user);
//									removeSession(oldSession);
//								}
//							});
//				} else if ("logout".equals(op)) {
//					oldSession.getHandshaker().close(
//							channel,
//							new CloseWebSocketFrame(1000, "the uid:"
//									+ oldSession.getUserId() + " is logout"));
//				}
//				return;
//			}
//			return;
//		}
//
//		if (frame instanceof BinaryWebSocketFrame) {
//			return;
//		}
//		if (frame instanceof PingWebSocketFrame) {
//			Statistics.increment(Statistics.CON_OUT_PING);
//			channel.write(new PongWebSocketFrame(frame.getBinaryData()));
//			return;
//		}
//		if (frame instanceof CloseWebSocketFrame) {
//			WebSocketServerHandshaker handshaker = session.getHandshaker();
//			if (handshaker != null) {
//				handshaker.close(channel, (CloseWebSocketFrame) frame);
//			} else {
//				channel.close();
//			}
//			return;
//		}
//
//		throw new UnsupportedOperationException(String.format(
//				"%s frame types not supported", frame.getClass().getName()));
//
//	}
//
//	private static void sendHttpResponse(ChannelHandlerContext ctx,
//			HttpRequest req, HttpResponse res) {
//		if (res.getStatus().getCode() != 200) {
//			res.setContent(ChannelBuffers.copiedBuffer(res.getStatus()
//					.toString(), CharsetUtil.UTF_8));
//			setContentLength(res, res.getContent().readableBytes());
//		}
//
//		ChannelFuture f = ctx.getChannel().write(res);
//		if (!isKeepAlive(req) || res.getStatus().getCode() != 200) {
//			f.addListener(ChannelFutureListener.CLOSE);
//		}
//	}
//
//	@Override
//	public void exceptionCaught(ChannelHandlerContext ctx, ExceptionEvent e)
//			throws Exception {
//		if (logger.isWarnEnabled()) {
//			logger.warn("the channel[" + ctx.getChannel()
//					+ "],the  exceptionCaught:", e.getCause());
//		}
//		e.getChannel().close();
//	}
//
//	@Override
//	public void channelClosed(ChannelHandlerContext ctx, ChannelStateEvent event)
//			throws Exception {
//		Statistics.increment(Statistics.CON_OUTH_CLOSE);
//		if (logger.isInfoEnabled()) {
//			logger.info(" the channel[" + ctx.getChannel() + "] was closed.");
//		}
//		Channel channel = ctx.getChannel();
//		if (DUPLICATED_CONNECT.equals(channel.getAttachment())) {
//			return;
//		}
//		Object object = ctx.getAttachment();
//		if (object == null || !(object instanceof Session)) {
//			if (logger.isWarnEnabled()) {
//				logger.warn("the context's attachment[" + object
//						+ "] is illegal");
//			}
//			return;
//		}
//		Session session = (Session) object;
//		removeSession(session);
//	}
//
//	private void removeSession(final Session session) {
//		sessionManager.removeSession(session);
//		// 同步tair
//		try {
//			cacheService.sDeleteAsync(session.getUserId(),
//					this.innerLocalAddressAndTime, -1, new CacheCallback() {
//						public void callback(boolean success, int code,
//								String message) {
//							if (!success) {
//								if (logger.isWarnEnabled()) {
//									logger.warn(
//											"can't delete channel from tair, code:{} , message:{}",
//											code, message);
//								}
//							}
//						}
//					});
//		} catch (Exception e) {
//			if (logger.isErrorEnabled()) {
//				logger.error("can't delete channel from tair,the error:" + e);
//			}
//		}
//	}
//
//	@Override
//	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e)
//			throws Exception {
//		Statistics.increment(Statistics.CON_OUTH_OPEN);
//		SocketAddress address = e.getChannel().getRemoteAddress();
//		if (address instanceof InetSocketAddress) {
//			InetSocketAddress inetAddresses = (InetSocketAddress) address;
//
//			if (blacklistService.existIp(inetAddresses.getAddress()
//					.getHostAddress())) {
//				if (logger.isWarnEnabled()) {
//					logger.warn("the ip:" + inetAddresses.getHostName()
//							+ " in blacklist ");
//				}
//				ctx.getChannel().close();
//			}
//		}
//	}
//
//	private static String getWebSocketLocation(HttpRequest req) {
//		return "ws://" + req.getHeader(HOST) + WEBSOCKET_PATH;
//	}
//
//	public void setSessionManager(SessionManager sessionManager) {
//		this.sessionManager = sessionManager;
//	}
//
//	public void setCacheService(CacheService cacheService) {
//		this.cacheService = cacheService;
//	}
//
//	public void setConfiguration(ServerConfiguration configuration) {
//		this.configuration = configuration;
//	}
//
//	public void setAuthService(AuthService authService) {
//		this.authService = authService;
//	}
//
//	public void setBlacklistService(BlacklistService blacklistService) {
//		this.blacklistService = blacklistService;
//	}
//
//}
