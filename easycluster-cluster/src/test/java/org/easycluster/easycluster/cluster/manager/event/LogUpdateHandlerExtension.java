package org.easycluster.easycluster.cluster.manager.event;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.easycluster.easycluster.core.Closure;
import org.junit.Assert;

public class LogUpdateHandlerExtension implements Closure {

	public static final Logger	LOGGER	= Logger.getLogger(LogUpdateHandlerExtension.class.getName());

	public LogUpdateHandlerExtension(EventHandler eventHandler) {
		LOGGER.setLevel(Level.INFO);
		final String eventType = EventType.LOG_UPDATE.name();

		eventHandler.registerObserver(eventType, new Closure() {

			@Override
			public void execute(Object msg) {
				Assert.assertTrue(msg instanceof LogUpdateEvent);
				Assert.assertEquals(eventType, ((LogUpdateEvent) msg).getType());
			}
		});
	}

	@Override
	public void execute(Object input) {
		if (LOGGER.isLoggable(Level.INFO)) {
			LOGGER.log(Level.INFO, "try echo:" + input);
		}

	}

}
