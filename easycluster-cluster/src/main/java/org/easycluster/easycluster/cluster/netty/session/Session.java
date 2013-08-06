package org.easycluster.easycluster.cluster.netty.session;

import org.jboss.netty.channel.Channel;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;

public class Session {

	private String						sn;

	private int							heartbeatInterval;

	private WebSocketServerHandshaker	handshaker;

	private Channel						channel;

	private long						lastLiveTime	= System.currentTimeMillis();

	private String						userId;

	private String						os;

	public Session(User user, Channel channel, WebSocketServerHandshaker handshaker) {
		if (user == null || channel == null || handshaker == null) {
			throw new IllegalArgumentException("user == null || channel == null || handshaker == null");
		}
		this.userId = user.getId();
		this.heartbeatInterval = user.getHeartbeatInterval();
		this.handshaker = handshaker;
		this.channel = channel;
		this.sn = user.getSn();
		this.os = user.getOs();
	}

	public WebSocketServerHandshaker getHandshaker() {
		return handshaker;
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public boolean healthExamine(long current) {
		if (current - lastLiveTime > heartbeatInterval) {
			return false;
		}
		return true;
	}

	public void updateLastLiveTime() {
		this.lastLiveTime = System.currentTimeMillis();
	}

	public String getUserId() {
		return userId;
	}

	public Channel getChannel() {
		return channel;
	}

	public String getSn() {
		return sn;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

}
