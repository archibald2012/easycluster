package org.easycluster.easycluster.cluster.netty.session;

import java.util.Collection;

import org.jboss.netty.channel.Channel;

public interface SessionManager {

	Collection<Session> getSession(String uid);

	Session putSession(Session newSession);

	void removeSession(Session session);

	Channel getChannel(int id);

	Channel putChannel(int id, Channel channel);

	Channel removeChannel(int id);

	Channel randomChannel();

}
