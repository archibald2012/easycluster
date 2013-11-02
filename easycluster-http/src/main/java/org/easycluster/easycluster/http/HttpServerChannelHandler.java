package org.easycluster.easycluster.http;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.endpoint.DefaultEndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.Endpoint;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointListener;
import org.easycluster.easycluster.cluster.server.MessageClosureRegistry;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
import org.easycluster.easycluster.core.Closure;
import org.easycluster.easycluster.core.KeyTransformer;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.core.TransportUtil;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServerChannelHandler extends IdleStateAwareChannelUpstreamHandler {

	private static final Logger					LOGGER					= LoggerFactory.getLogger(HttpServerChannelHandler.class);

	private ChannelGroup						channelGroup			= null;
	private MessageClosureRegistry				messageHandlerRegistry	= null;
	private MessageExecutor						messageExecutor			= null;
	private KeyTransformer						keyTransformer			= new HttpKeyTransformer();
	private EndpointFactory						endpointFactory			= new DefaultEndpointFactory();
	private final ChannelLocal<Endpoint>		endpoints				= new ChannelLocal<Endpoint>();
	private Transformer<HttpRequest, Object>	requestTransformer		= null;
	private Transformer<Object, HttpResponse>	responseTransformer		= null;
	private HttpResponseSender					responseSender			= new HttpResponseSender();

	public HttpServerChannelHandler(ChannelGroup channelGroup, MessageClosureRegistry messageHandlerRegistry, MessageExecutor messageExecutor) {
		this.channelGroup = channelGroup;
		this.messageHandlerRegistry = messageHandlerRegistry;
		this.messageExecutor = messageExecutor;
	}

	@Override
	public void channelOpen(ChannelHandlerContext ctx, ChannelStateEvent e) {
		Channel channel = e.getChannel();
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("channelOpen: " + channel);
		}
		Endpoint endpoint = endpointFactory.createEndpoint(e.getChannel());
		if (null != endpoint) {
			attachEndpointToSession(e.getChannel(), endpoint);
		}
		channelGroup.add(channel);

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
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) {
		if (LOGGER.isTraceEnabled()) {
			LOGGER.trace("message received {}", e.getMessage());
		}

		Channel channel = e.getChannel();
		HttpRequest request = (HttpRequest) e.getMessage();

		Endpoint endpoint = getEndpointOfSession(channel);

		if (endpoint == null) {
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e1) {
				LOGGER.error("", e1);
			}
			endpoint = getEndpointOfSession(channel);
		}

		if (null != endpoint) {

			Object signal = null;
			try {
				signal = requestTransformer.transform(request);
			} catch (Exception ex) {
				LOGGER.error("", ex);
				HttpResponse resp = ConstantResponse.RESPONSE_400_NOBODY;
				String uuid = request.getHeader(NettyConstants.HEADER_UUID);
				if (uuid != null) {
					resp.setHeader(NettyConstants.HEADER_UUID, uuid);
				}
				responseSender.sendResponse(channel, resp);
				return;
			}
			if (signal == null) {
				return;
			}

			TransportUtil.attachSender(signal, endpoint);

			HttpResponseHandler responseHandler = new HttpResponseHandler(channel, request, signal);

			if (!messageHandlerRegistry.messageRegistered(signal.getClass())) {
				String error = String.format("No such message of type %s registered", signal.getClass().getName());
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn(error);
				}
				responseHandler.execute(new InvalidMessageException(error));
			} else {
				messageExecutor.execute(signal, responseHandler);
			}

		} else {
			if (LOGGER.isWarnEnabled()) {
				LOGGER.warn("missing endpoint, ignore incoming msg:", request);
			}
			// error reactor
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

	class HttpResponseHandler implements Closure {
		private Channel		channel;
		private HttpRequest	request;
		private Object		signal;

		public HttpResponseHandler(Channel channel, HttpRequest request, Object signal) {
			this.channel = channel;
			this.request = request;
			this.signal = signal;
		}

		@Override
		public void execute(Object message) {

			HttpResponse response = null;
			if (message instanceof Exception) {
				message = buildErrorResponse((Exception) message);
				response = responseTransformer.transform(message);
				response.setStatus(HttpResponseStatus.INTERNAL_SERVER_ERROR);
			} else {
				response = responseTransformer.transform(message);
				response.setStatus(HttpResponseStatus.OK);
			}

			Object requestId = keyTransformer.transform(request);
			if (requestId != null) {
				response.setHeader(NettyConstants.HEADER_UUID, requestId);
			}

			String keepAlive = request.getHeader(HttpHeaders.Names.CONNECTION);
			if (keepAlive != null) {
				response.setHeader(HttpHeaders.Names.CONNECTION, keepAlive);
			}

			channel.write(response);

			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("Sent http response: {}", response);
			}
		}

		private Object buildErrorResponse(Exception ex) {
			Class<?> responseType = messageHandlerRegistry.getResponseTypeFor(signal);
			if (responseType == null) {
				return null;
			}

			Object response = null;
			try {
				response = responseType.newInstance();
				// TODO set exception message
			} catch (Exception e) {
				if (LOGGER.isWarnEnabled()) {
					LOGGER.warn("Build default response with error " + e.getMessage(), e);
				}
			}
			return response;
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

	public void setRequestTransformer(Transformer<HttpRequest, Object> requestTransformer) {
		this.requestTransformer = requestTransformer;
	}

	public void setResponseTransformer(Transformer<Object, HttpResponse> responseTransformer) {
		this.responseTransformer = responseTransformer;
	}

	public void setResponseSender(HttpResponseSender responseSender) {
		this.responseSender = responseSender;
	}

}
