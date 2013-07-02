package org.easycluster.easycluster.cluster.common;

import java.util.TimerTask;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicLong;

public class RequestsTracker {

	private AtomicLong					handledTransaction		= new AtomicLong(0);
	private AtomicLong					finishedTransaction		= new AtomicLong(0);
	private long						checkInterval			= 1000;
	private long						lastTimestamp			= 0;
	private long						lastHandledTransaction	= 0;
	private long						lastFinishedTransaction	= 0;
	private AverageTracker				handledThroughput		= new AverageTracker(100);
	private AverageTracker				finishedThroughput		= new AverageTracker(100);

	private ScheduledExecutorService	exec					= Executors.newSingleThreadScheduledExecutor();

	public void start() {
		exec.scheduleAtFixedRate(new TimerTask() {

			@Override
			public void run() {
				long now = System.currentTimeMillis();
				long interval = now - lastTimestamp;

				if (interval > 0) {// 避免除0
					calculatePerformance(interval);
				}
				lastTimestamp = now;
			}
		}, checkInterval, checkInterval, TimeUnit.MILLISECONDS);
	}

	public void stop() {
		exec.shutdownNow();
	}

	public void increaseRequested() {
		handledTransaction.incrementAndGet();
	}

	public void increaseFinished() {
		finishedTransaction.incrementAndGet();
	}

	private void calculatePerformance(long interval) {
		long handledTransactionNow = handledTransaction.get();
		long finishedTransactionNow = finishedTransaction.get();

		handledThroughput.add((handledTransactionNow - lastHandledTransaction) * 1000 / interval);
		lastHandledTransaction = handledTransactionNow;

		finishedThroughput.add((finishedTransactionNow - lastFinishedTransaction) * 1000 / interval);
		lastFinishedTransaction = finishedTransactionNow;
	}

	public void setCheckInterval(long checkInterval) {
		this.checkInterval = checkInterval;
	}

	public long getHandledTransaction() {
		return handledTransaction.get();
	}

	public long getFinishedTransaction() {
		return finishedTransaction.get();
	}

	public double getHandledThroughput() {
		return handledThroughput.getAverage();
	}

	public double getFinishedThroughput() {
		return finishedThroughput.getAverage();
	}

}
