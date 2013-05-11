package org.easycluster.easycluster.cluster.netty;

import org.easycluster.easycluster.cluster.common.TransportUtil;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.endpoint.DefaultEndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.Endpoint;
import org.easycluster.easycluster.cluster.netty.endpoint.EndpointFactory;
import org.easycluster.easycluster.cluster.netty.endpoint.IEndpointListener;
import org.easycluster.easycluster.cluster.server.MessageClosureRegistry;
import org.easycluster.easycluster.cluster.server.MessageExecutor;
import org.easycluster.easycluster.core.Closure;
import org.easycluster.easycluster.core.Identifiable;
import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ChannelLocal;
import org.jboss.netty.channel.ChannelStateEvent;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.group.ChannelGroup;
import org.jboss.netty.handler.timeout.IdleStateAwareChannelUpstreamHandler;
import org.jboss.netty.handler.timeout.IdleStateEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ServerChannelHandler extends IdleStateAwareChannelUpstreamHandler {

	private static final Logger			LOGGER					= LoggerFactory.getLogger(ServerChannelHandler.class);

	private ChannelGroup				channelGroup			= null;
	private MessageClosureRegistry		messageHandlerRegistry	= null;
	private MessageExecutor				messageExecutor			= null;
	private KeyTransformer				keyTransformer			= new KeyTransformer();
	private EndpointFactory				endpointFactory			= new DefaultEndpointFactory();

	public final ChannelLocal<Endpoint>	endpoints				= new ChannelLocal<Endpoint>();

	public ServerChannelHandler(ChannelGroup channelGroup, MessageClosureRegistry messageHandlerRegistry, MessageExecutor messageExecutor) {
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
		LOGGER.error("channel: [" + e.getChannel().getRemoteAddress() + "], exceptionCaught: [{}]", e.getCause());
		ctx.getChannel().close();
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
		Object request = e.getMessage();

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
			TransportUtil.attachSender(request, endpoint);

			ResponseHandler responseHandler = new ResponseHandler(endpoint, request);

			if (!messageHandlerRegistry.messageRegistered(request.getClass())) {
				String error = String.format("No such message of type %s registered", request.getClass().getName());
				LOGGER.warn(error);
				responseHandler.execute(new InvalidMessageException(error));
			} else {
				messageExecutor.execute(request, responseHandler);
			}

		} else {
			LOGGER.warn("missing endpoint, ignore incoming msg:", request);
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

	class ResponseHandler implements Closure {
		private Endpoint	endpoint;
		private Object		request;
		private long		requestId;

		public ResponseHandler(Endpoint endpoint, Object request) {
			this.endpoint = endpoint;
			this.request = request;
			this.requestId = (Long) keyTransformer.transform(request);
		}

		@Override
		public void execute(Object message) {
			if (message instanceof Exception) {
				Exception ex = (Exception) message;
				message = buildErrorResponse(ex);
			}

			doSend(message);
		}

		private Object buildErrorResponse(Exception ex) {
			Class<?> responseType = messageHandlerRegistry.getResponseFor(request);
			if (responseType == null) {
				return null;
			}

			Object response = null;
			try {
				response = responseType.newInstance();
				// TODO set exception message
			} catch (Exception e) {
				LOGGER.warn("Build default response with error " + e.getMessage(), e);
			}
			return response;
		}

		private void doSend(Object message) {
			if (message != null) {

				if (message instanceof Identifiable) {
					((Identifiable) message).setIdentification(requestId);
				}

				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("send - [{}]", message);
				}
				endpoint.send(message);
			}
		}
	}

	public void setEndpointFactory(EndpointFactory endpointFactory) {
		this.endpointFactory = endpointFactory;
	}

	public void setEndpointListener(IEndpointListener endpointListener) {
		this.endpointFactory.setEndpointListener(endpointListener);
	}

	public void setKeyTransformer(KeyTransformer keyTransformer) {
		this.keyTransformer = keyTransformer;
	}

}
