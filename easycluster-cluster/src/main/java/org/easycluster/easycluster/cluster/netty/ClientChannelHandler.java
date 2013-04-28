package org.easycluster.easycluster.cluster.netty;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.common.AverageTimeTracker;
import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.common.RequestsPerSecondTracker;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.KeyTransformer;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.ExceptionEvent;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientChannelHandler extends SimpleChannelHandler {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(ClientChannelHandler.class);

	private ConcurrentHashMap<Object, MessageContext> requestMap = new ConcurrentHashMap<Object, MessageContext>();
	private MessageRegistry messageRegistry;

	private AverageTimeTracker processingTime = new AverageTimeTracker(100);
	private RequestsPerSecondTracker rps = new RequestsPerSecondTracker();
	private KeyTransformer keyTransformer = new KeyTransformer();

	public ClientChannelHandler(MessageRegistry messageRegistry,
			final int staleRequestTimeoutMins,
			final int staleRequestCleanupFrequencyMins) {
		this.messageRegistry = messageRegistry;

		Thread cleanupThread = new Thread("stale-request-cleanup-thread") {
			long staleRequestTimeoutMillis = TimeUnit.MILLISECONDS.convert(
					staleRequestTimeoutMins, TimeUnit.MINUTES);

			public void run() {
				while (true) {
					try {
						TimeUnit.MINUTES
								.sleep(staleRequestCleanupFrequencyMins);
					} catch (InterruptedException e) {
						continue;
					}

					for (Object key : requestMap.keySet()) {
						MessageContext request = requestMap.get(key);
						if ((System.currentTimeMillis() - request
								.getTimestamp()) > staleRequestTimeoutMillis) {
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
	public void writeRequested(ChannelHandlerContext ctx, MessageEvent e)
			throws Exception {
		MessageContext requestContext = (MessageContext) e.getMessage();
		Object request = requestContext.getMessage();
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("Writing request: {}", request);
		}

		if (messageRegistry.hasResponse(request.getClass())) {
			Object requestId = keyTransformer.transform(request);
			requestMap.put(requestId, requestContext);
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
		MessageContext requestContext = requestMap.get(requestId);
		if (requestContext == null) {
			LOGGER.warn(
					"Received a response message [%s] without a corresponding request",
					message);
		} else {
			requestMap.remove(requestId);

			Class<? extends Object> requestType = requestContext.getMessage()
					.getClass();

			if (messageRegistry.validResponseFor(requestType, message)) {
				requestContext.getClosure().execute(message);
			} else {
				throw new InvalidMessageException(
						String.format(
								"Response message of type %s doesn't match registered response for %s",
								message.getClass().getName(),
								requestType.getName()));
			}

		}
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
