package org.easycluster.easycluster.core.ebus;

import java.util.ArrayList;
import java.util.List;

public class Subscription {

	private String			event;
	private List<Clojure>	clojures	= new ArrayList<Clojure>();

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

	public void setClojures(List<Clojure> clojures) {
		this.clojures = clojures;
	}

	public void addClojure(Clojure clojure) {
		if (!getClojures().contains(clojure)) {
			getClojures().add(clojure);
		}
	}

	public void removeClojure(Clojure clojure) {
		getClojures().remove(clojure);
	}

	public int size() {
		return getClojures().size();
	}

	public List<Clojure> getClojures() {
		if (clojures == null) {
			clojures = new ArrayList<Clojure>();
		}
		return clojures;
	}

	public void execute(Object... args) {
		for (Clojure clojure : getClojures()) {
			clojure.execute(args);
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		for (Clojure clojure : clojures) {
			sb.append(clojure.toString());
			sb.append(";");
		}
		return sb.toString();
	}

}