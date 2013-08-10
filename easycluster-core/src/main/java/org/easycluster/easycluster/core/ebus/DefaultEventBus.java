package org.easycluster.easycluster.core.ebus;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;

import org.easycluster.easycluster.core.Closure;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Single threaded event bus.
 * 
 * @author wangqi
 * @version $Id: DefaultEventBus.java 34 2012-01-17 11:03:56Z archie $
 */
public class DefaultEventBus implements EventBus {

	private static final Logger						logger			= LoggerFactory.getLogger(DefaultEventBus.class);
	private ExecutorService							mainExecutor	= null;

	/**
	 * Map of event type to subscribers to the event type.
	 */
	private ConcurrentHashMap<String, Subscription>	subscriptions	= new ConcurrentHashMap<String, Subscription>();

	public void start() {
	}

	public void destroy() {
		Collection<Subscription> subs = this.subscriptions.values();
		for (Subscription sub : subs) {
			for (Closure closure : sub.getClosures()) {
				if (null != closure) {
					// TODO need set cancel flat to closure?
					unsubscribe(sub.getEvent(), closure);
				}
			}
		}
		this.subscriptions.clear();

		if (mainExecutor != null) {
			mainExecutor.shutdownNow();
		}
	}

	@Override
	public void subscribe(final String event, final Closure closure) {
		if (event == null || closure == null) {
			throw new IllegalArgumentException("event [" + event + "], closure [" + closure + "]");
		}
		getOrCreateSubscription(event).addClosure(closure);
	}

	@Override
	public void unsubscribe(String event, Closure closure) {
		if (event == null || closure == null) {
			throw new IllegalArgumentException("event [" + event + "], closure [" + closure + "]");
		}

		Subscription subscription = getSubscription(event);
		if (subscription == null) {
			logger.info("no subscription for event {}", event);
			return;
		}
		subscription.removeClosure(closure);

	}

	@Override
	public void publish(final String event, final Object... args) {
		if (this.mainExecutor != null) {
			this.mainExecutor.submit(new Runnable() {
				public void run() {
					doPublishEvent(event, args);
				}
			});
		} else {
			doPublishEvent(event, args);
		}
	}

	private Subscription getOrCreateSubscription(String event) {
		Subscription subscription = subscriptions.get(event);

		if (null == subscription) {
			subscription = new Subscription();
			subscription.setEvent(event);
			subscriptions.put(event, subscription);
		}

		return subscription;
	}

	public Subscription getSubscription(String event) {
		return subscriptions.get(event);
	}

	public Map<String, Subscription> getSubscriptions() {
		return subscriptions;
	}

	private void doPublishEvent(final String event, final Object... args) {
		Subscription subscription = this.getSubscription(event);

		// are there subscribers for this event?
		if (null != subscription && subscription.size() > 0) {
			subscription.execute(args);
		} else {
			logger.warn("no subscription for event [" + event + "]!");
		}
	}

	public int getPendingTaskCount() {
		if (this.mainExecutor == null) {
			return 0;
		}
		if (this.mainExecutor instanceof ThreadPoolExecutor) {
			BlockingQueue<Runnable> queue = ((ThreadPoolExecutor) mainExecutor).getQueue();
			return queue.size();
		} else {
			throw new RuntimeException("Internal Erro : mainExecutor is !NOT! ThreadPoolExecutor class");
		}
	}

	public void setThreadSize(int nThreads) {
		this.mainExecutor = Executors.newFixedThreadPool(nThreads, new ThreadFactory() {
			public Thread newThread(Runnable r) {
				return new Thread(r, "ebus main threads");
			}
		});
	}

}
