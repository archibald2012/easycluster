package org.easycluster.easycluster.core.ebus;

import java.util.ArrayList;
import java.util.List;

import org.easycluster.easycluster.core.Closure;

public class Subscription {

	private String event;
	private List<Closure> closures = new ArrayList<Closure>();

	public Subscription() {
	}

	public Subscription(String event) {
		this.event = event;
	}

	public String getEvent() {
		return event;
	}

	public void setEvent(String event) {
		this.event = event;
	}

	public void setClosures(List<Closure> closures) {
		this.closures = closures;
	}

	public void addClosure(Closure closure) {
		if (!getClosures().contains(closure)) {
			getClosures().add(closure);
		}
	}

	public void removeClosure(Closure closure) {
		getClosures().remove(closure);
	}

	public int size() {
		return getClosures().size();
	}

	public List<Closure> getClosures() {
		if (closures == null) {
			closures = new ArrayList<Closure>();
		}
		return closures;
	}

	public void execute(Object... args) {
		for (Closure closure : getClosures()) {
			closure.execute(args);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Closure closure : closures) {
			sb.append(closure.toString());
			sb.append(";");
		}
		return sb.toString();
	}

}