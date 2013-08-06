package org.easycluster.easycluster.cluster.netty.session;

public  class User {

	private String id;
	private String sn;
	private String os;
	private String token;
	private int heartbeatInterval = 85000;
	boolean newSn;
	private String authType;
	private String authStr;

	public User(String uid, String sn, String os, boolean newSn) {
		this.id = uid;
		this.sn = sn;
		this.os = os;
		this.newSn = newSn;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getSn() {
		return sn;
	}

	public void setSn(String sn) {
		this.sn = sn;
	}

	public String getOs() {
		return os;
	}

	public void setOs(String os) {
		this.os = os;
	}

	public int getHeartbeatInterval() {
		return heartbeatInterval;
	}

	public void setHeartbeatInterval(int heartbeatInterval) {
		this.heartbeatInterval = heartbeatInterval;
	}

	public boolean isNewSn() {
		return newSn;
	}

	public void setNewSn(boolean newSn) {
		this.newSn = newSn;
	}

	public void setAuthType(String authType) {
		this.authType = authType;
	}

	public String getAuthType() {
		return authType;
	}

	public void setAuthStr(String authStr) {
		this.authStr = authStr;
	}

	public String getAuthStr() {
		return authStr;
	}

	public String getToken() {
		return token;
	}

	public void setToken(String token) {
		this.token = token;
	}

	public String toString() {
		return "uid:" + id + ",sn:" + sn + ",os:" + os + "";
	}
}
