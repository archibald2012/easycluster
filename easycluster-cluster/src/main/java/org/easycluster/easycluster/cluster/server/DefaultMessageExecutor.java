package org.easycluster.easycluster.cluster.server;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DefaultMessageExecutor implements MessageExecutor {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(DefaultMessageExecutor.class);

	private MessageClosureRegistry	messageHandlerRegistry	= null;

	public DefaultMessageExecutor(MessageClosureRegistry messageHandlerRegistry) {
		this.messageHandlerRegistry = messageHandlerRegistry;
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void execute(Object message, Closure closure) {
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(String.format("Executing message: %s", message));
		}

		try {
			MessageClosure handler = messageHandlerRegistry.getHandlerFor(message);
			Object response = handler.execute(message);
			if (!messageHandlerRegistry.validResponseFor(message, response)) {
				String name = (response == null) ? "<null>" : response.getClass().getName();
				String errorMsg = String.format("Message handler returned an invalid response message of type %s", name);
				LOGGER.error(errorMsg);
				closure.execute(new InvalidMessageException(errorMsg));
			} else {
				closure.execute(response);
			}
		} catch (InvalidMessageException ex) {
			LOGGER.error(String.format("Received an invalid message: %s", message));
			closure.execute(ex);
		} catch (Exception ex) {
			LOGGER.error("Message handler threw an exception while processing message", ex);
			closure.execute(ex);
		}
	}

	@Override
	public void shutdown() {
		// unregister jmx
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("MessageExecutor shut down");
		}
	}

}
