package org.easycluster.easycluster.cluster.server;

import java.lang.management.ManagementFactory;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

import javax.management.MBeanServer;
import javax.management.ObjectName;
import javax.management.StandardMBean;

import org.easycluster.easycluster.cluster.common.AverageTracker;
import org.easycluster.easycluster.cluster.common.NamedPoolThreadFactory;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ThreadPoolMessageExecutor implements MessageExecutor {

	private static final Logger		LOGGER					= LoggerFactory.getLogger(ThreadPoolMessageExecutor.class);

	private MessageClosureRegistry	messageHandlerRegistry	= null;
	private ThreadPoolExecutor		threadPool				= null;

	private String					mbeanObjectName			= "org.easycluster:type=ThreadPoolMessageExecutor,name=%s";
	private AverageTracker			waitTime				= new AverageTracker(100);
	private AverageTracker			processingTime			= new AverageTracker(100);
	private AtomicLong				requestCount			= new AtomicLong(0);

	public ThreadPoolMessageExecutor(String name, int corePoolSize, int maxPoolSize, int keepAliveTime, MessageClosureRegistry messageHandlerRegistry) {

		this.messageHandlerRegistry = messageHandlerRegistry;

		this.threadPool = new ThreadPoolExecutor(corePoolSize, maxPoolSize, keepAliveTime, TimeUnit.SECONDS, new LinkedBlockingQueue<Runnable>(),
				new NamedPoolThreadFactory(name)) {

			@Override
			public void beforeExecute(Thread t, Runnable r) {
				RequestRunner rr = (RequestRunner) r;
				rr.startedAt = System.nanoTime();
				waitTime.add(rr.startedAt - rr.queuedAt);
				requestCount.incrementAndGet();
			}

			@Override
			public void afterExecute(Runnable r, Throwable t) {
				processingTime.add((System.nanoTime() - ((RequestRunner) r).startedAt));
			}
		};

		MBeanServer mbeanServer = ManagementFactory.getPlatformMBeanServer();
		try {
			ObjectName measurementName = new ObjectName(String.format(mbeanObjectName, name));
			if (mbeanServer.isRegistered(measurementName)) {
				mbeanServer.unregisterMBean(measurementName);
			}
			StandardMBean requestProcessorMBean = new StandardMBean(new ThreadPoolMessageExecutorMBean() {
				public int getQueueSize() {
					return threadPool.getQueue().size();
				}

				public int getPoolSize() {
					return threadPool.getPoolSize();
				}

				public int getCorePoolSize() {
					return threadPool.getCorePoolSize();
				}

				public int getMaxPoolSize() {
					return threadPool.getMaximumPoolSize();
				}

				public int getLargestPoolSize() {
					return threadPool.getLargestPoolSize();
				}

				public long getKeepAliveTime() {
					return threadPool.getKeepAliveTime(TimeUnit.MILLISECONDS);
				}

				public int getActiveCount() {
					return threadPool.getActiveCount();
				}

				public long getCompletedTaskCount() {
					return threadPool.getCompletedTaskCount();
				}

				public double getAverageWaitTimeInMillis() {
					return waitTime.getAverage() / 1000000;
				}

				public double getAverageProcessingTimeInMillis() {
					return processingTime.getAverage() / 1000000;
				}

				public long getRequestCount() {
					return requestCount.get();
				}
			}, ThreadPoolMessageExecutorMBean.class);

			mbeanServer.registerMBean(requestProcessorMBean, measurementName);

			if (LOGGER.isInfoEnabled()) {
				LOGGER.info("Registering with JMX server as MBean [" + measurementName + "]");
			}
		} catch (Exception e) {
			String message = "Unable to register MBeans with error " + e.getMessage();
			LOGGER.error(message, e);
		}
	}

	@Override
	public void execute(Object message, Closure closure) {
		threadPool.execute(new RequestRunner(message, closure, System.nanoTime()));
	}

	@Override
	public void shutdown() {
		threadPool.shutdown();
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

}
