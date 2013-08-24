package org.easycluster.easycluster.cluster.netty;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

import org.easycluster.easycluster.cluster.common.MessageContext;
import org.easycluster.easycluster.cluster.common.MessageRegistry;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MessageContextHolder {

	private static final Logger			LOGGER			= LoggerFactory.getLogger(MessageContextHolder.class);

	private Map<Object, MessageContext>	requestMap		= new ConcurrentHashMap<Object, MessageContext>();
	private MessageRegistry				messageRegistry	= null;

	public MessageContextHolder(MessageRegistry messageRegistry, final int staleRequestTimeoutMins, final int staleRequestCleanupFrequencyMins) {
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
						if ((System.nanoTime() - request.getTimestamp()) / 1000 * 1000 > staleRequestTimeoutMillis) {
							LOGGER.warn("Remove timeout message context. key=[{}], timeoutMillis=[{}]", key, staleRequestTimeoutMillis);
							requestMap.remove(key);
						}
					}

				}
			}

		};

		cleanupThread.setDaemon(true);
		cleanupThread.start();
	}

	public void add(Object requestId, MessageContext requestContext) {
		Object message = requestContext.getMessage();
		if (!messageRegistry.hasResponse(message.getClass())) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("No response needed for request object {}.", message);
			}
			return;
		}
		if (requestId == null) {
			LOGGER.warn("No request id found from request object {}.", message);
			return;
		}
		if (requestMap.containsKey(requestId)) {
			LOGGER.warn("Duplicated request id found from request object {}.", message);
			return;
		}
		requestMap.put(requestId, requestContext);
	}

	public MessageContext remove(Object requestId, Object response) {
		MessageContext requestContext = null;
		if (requestId != null) {
			requestContext = requestMap.remove(requestId);
			if (requestContext == null) {
				LOGGER.warn("Received a response message [{}] without a corresponding request", response);
			} else {
				Class<? extends Object> requestType = requestContext.getMessage().getClass();
				if (!messageRegistry.validResponseFor(requestType, response)) {
					throw new InvalidMessageException(String.format("Response message of type %s doesn't match registered response for %s", response.getClass()
							.getName(), requestType.getName()));
				}
			}
		} else {
			LOGGER.warn("No request id found from response object {}.", response);
		}
		return requestContext;
	}

	public boolean containsKey(Object requestId) {
		return requestMap.containsKey(requestId);
	}
}
