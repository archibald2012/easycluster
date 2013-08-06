package org.easycluster.easycluster.cluster.netty.session;

import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.atomic.AtomicLong;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SessionManagerImpl implements SessionManager {

	private final static Logger										logger			= LoggerFactory.getLogger(SessionManagerImpl.class);

	private ConcurrentMap<String, ConcurrentMap<String, Session>>	outerSessions	= new ConcurrentHashMap<String, ConcurrentMap<String, Session>>(10000);

	private ConcurrentMap<Integer, Channel>							innerChannels	= new ConcurrentHashMap<Integer, Channel>(100);

	private long													period			= 90 * 1000;

	private AtomicLong												randomTimes		= new AtomicLong();

	private Timer													timer;

	private boolean													shutdown;

	public void init() {
		timer = new Timer("SessionManagerImpl.timer", true);
		timer.schedule(new TimerTask() {

			@Override
			public void run() {
				try {
					int removeUserTimes = 0;
					int removeSessionTimes = 0;
					Iterator<Map.Entry<String, ConcurrentMap<String, Session>>> outerIt = outerSessions.entrySet().iterator();
					long current = System.currentTimeMillis();
					while (outerIt.hasNext()) {
						Map.Entry<String, ConcurrentMap<String, Session>> entry = outerIt.next();
						if (entry.getValue() == null || entry.getValue().isEmpty()) {
							outerSessions.remove(entry.getKey(), entry.getValue());
							removeUserTimes++;
							continue;
						}
						Iterator<Session> it = entry.getValue().values().iterator();
						while (it.hasNext()) {
							Session session = it.next();
							if (session == null) {
								continue;
							}
							boolean healthy = session.healthExamine(current);
							if (!healthy) {
								it.remove();
								removeSessionTimes++;
								WebSocketServerHandshaker handshaker = session.getHandshaker();
								handshaker.close(session.getChannel(), new CloseWebSocketFrame(1000, "the server is closed!"));
							}
						}
					}
					long end = System.currentTimeMillis();
					if (logger.isWarnEnabled()) {
						logger.warn("health's  examine cost time:" + (end - current) + ",removeUserTimes:" + removeUserTimes + ",removeSessionTimes:"
								+ removeSessionTimes);
					}
				} catch (Exception e) {
					if (logger.isErrorEnabled()) {
						logger.error("SessionManagerImpl.timer catch exception:" + e);
					}
				}
			}

		}, period, period);
	}

	public void destroy() {
		shutdown = true;
		timer.purge();
		timer.cancel();
		// try {
		// Iterator<Entry<String, Collection<Session>>> outerIt = outerSessions
		// .entrySet().iterator();
		// while (outerIt.hasNext()) {
		// Entry<String, Collection<Session>> entry = outerIt.next();
		// Iterator<Session> it = entry.getValue().iterator();
		// while (it.hasNext()) {
		// Session session = it.next();
		// if (session != null) {
		// session.getHandshaker().close(session.getChannel(),
		// new CloseWebSocketFrame());
		// }
		// }
		// }
		//
		// Iterator<Entry<Integer, Channel>> innerIt = innerChannels
		// .entrySet().iterator();
		// while (innerIt.hasNext()) {
		// Entry<Integer, Channel> entry = innerIt.next();
		// innerIt.remove();
		// entry.getValue().close();
		// }
		// } catch (Exception e) {
		// if (logger.isErrorEnabled()) {
		// logger.error("SessionManagerImpl destroy Exception:{}", e);
		// }
		// }
		int times = 0;
		int interval = 500;
		while (!Thread.interrupted()) {
			try {
				Thread.sleep(interval);
				times++;
				if (logger.isWarnEnabled()) {
					logger.warn("..............closing................,times:" + times + ",outerSessions.size():" + outerSessions.size());
				}
			} catch (InterruptedException e) {
			}
			Iterator<Entry<String, ConcurrentMap<String, Session>>> outerIt = outerSessions.entrySet().iterator();
			while (outerIt.hasNext()) {
				Entry<String, ConcurrentMap<String, Session>> entry = outerIt.next();
				if (entry.getValue() == null || entry.getValue().isEmpty()) {
					outerIt.remove();
				}
			}
			if (outerSessions.isEmpty() || times >= (2 * 180)) {
				break;
			}
		}
		if (logger.isWarnEnabled()) {
			logger.warn("..............closed................,times:" + times + ",outerSessions.size():" + outerSessions.size());
		}
	}

	public Collection<Session> getSession(String userId) {
		if (userId == null) {
			return null;
		}
		// if (shutdown) {
		// throw new IllegalStateException("the server is shutdown!");
		// }
		ConcurrentMap<String, Session> map = outerSessions.get(userId);
		if (map == null || map.isEmpty()) {
			return null;
		}
		return map.values();
	}

	public Session putSession(Session newSession) {
		if (shutdown) {
			throw new IllegalStateException("the server is shutdown!");
		}
		ConcurrentMap<String, Session> sessions = outerSessions.get(newSession.getUserId());
		if (sessions == null) {
			ConcurrentMap<String, Session> newSessions = new ConcurrentHashMap<String, Session>();
			ConcurrentMap<String, Session> oldSessions = outerSessions.putIfAbsent(newSession.getUserId(), newSessions);
			if (oldSessions != null) {
				sessions = oldSessions;
			} else {
				sessions = newSessions;
			}
		}
		return sessions.put(newSession.getSn(), newSession);
	}

	public void removeSession(Session session) {
		if (session == null) {
			return;
		}
		String uid = session.getUserId();
		ConcurrentMap<String, Session> sessions = outerSessions.get(uid);
		if (sessions == null || sessions.isEmpty()) {
			return;
		}
		sessions.remove(session.getSn(), session);
	}

	public Channel getChannel(int id) {
		return innerChannels.get(id);
	}

	public Channel putChannel(int id, Channel channel) {
		if (shutdown) {
			throw new IllegalStateException("the server is shutdown!");
		}
		return innerChannels.putIfAbsent(id, channel);
	}

	public Channel removeChannel(int id) {
		return innerChannels.remove(id);
	}

	public Channel randomChannel() {
		long l = randomTimes.incrementAndGet();
		if (l > (Long.MAX_VALUE - Integer.MAX_VALUE)) {
			randomTimes.set(0);
		}
		Collection<Channel> collection = innerChannels.values();
		int size = collection.size();
		if (size == 0) {
			if (logger.isWarnEnabled()) {
				logger.warn("the inner channle is empty!");
			}
			return null;
		}
		int index = (int) (l % size);
		for (Channel channel : collection) {
			if (index == 0) {
				return channel;
			}
			index--;
		}
		if (logger.isWarnEnabled()) {
			logger.warn("not find channel");
		}
		return collection.iterator().next();
	}

}
