package org.easycluster.easycluster.cluster.server;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import org.easycluster.easycluster.cluster.common.AverageTimeTracker;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolMessageExecutor implements MessageExecutor {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(ThreadPoolMessageExecutor.class);

	private MessageClosureRegistry	messageHandlerRegistry	= null;
	private ThreadPoolExecutor		threadPool				= null;

	private AverageTimeTracker		waitTime				= new AverageTimeTracker(100);
	private AverageTimeTracker		processingTime			= new AverageTimeTracker(100);
	private AtomicLong				requestCount			= new AtomicLong(0);

	public ThreadPoolMessageExecutor(String name, int corePoolSize, int maxPoolSize, int keepAliveTime, MessageClosureRegistry messageHandlerRegistry) {

		this.messageHandlerRegistry = messageHandlerRegistry;

		this.threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new NamedPoolThreadFactory(name)) {

			@Override
			public void beforeExecute(Thread t, Runnable r) {
				RequestRunner rr = (RequestRunner) r;
				rr.startedAt = System.currentTimeMillis();
				waitTime.addTime(rr.startedAt - rr.queuedAt);
				long requests = requestCount.incrementAndGet();
				if (LOGGER.isDebugEnabled()) {
					LOGGER.debug("requestCount: {}", requests);
				}
			}

			@Override
			public void afterExecute(Runnable r, Throwable t) {
				processingTime.add((System.currentTimeMillis() - ((RequestRunner) r).startedAt));
			}
		};

		// TODO register jmx
	}

	@Override
	public void execute(Object message, Closure closure) {
		threadPool.execute(new RequestRunner(message, closure, System.currentTimeMillis()));
	}

	@Override
	public void shutdown() {
		threadPool.shutdown();
		// unregister jmx
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("MessageExecutor shut down");
		}
	}

	class RequestRunner implements Runnable {
		private Object	message;
		private Closure	closure;
		private long	queuedAt;
		private long	startedAt;

		private RequestRunner(Object message, Closure closure, long queuedAt) {
			this.message = message;
			this.closure = closure;
			this.queuedAt = queuedAt;
		}

		@SuppressWarnings({ "rawtypes", "unchecked" })
		@Override
		public void run() {
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
				LOGGER.error(String.format("Received an invalid message: %s", message), ex);
				closure.execute(ex);
			} catch (Exception ex) {
				LOGGER.error("Message handler threw an exception while processing message", ex);
				closure.execute(ex);
			}

		}
	}

	class RequestProcessorMBean {

		public int getQueueSize() {
			return threadPool.getQueue().size();
		}

		public long getAverageWaitTime() {
			return waitTime.average();
		}

		public long getAverageProcessingTime() {
			return processingTime.average();
		}

		public long getRequestCount() {
			return requestCount.get();
		}
	}

}
