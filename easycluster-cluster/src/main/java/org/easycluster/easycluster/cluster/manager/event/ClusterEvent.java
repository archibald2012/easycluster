package org.easycluster.easycluster.cluster.manager.event;

public class ClusterEvent {

	private CoreEvent	event;

	public ClusterEvent() {
	}

	public ClusterEvent(CoreEvent event) {
		this.event = event;
	}

	public CoreEvent getEvent() {
		return event;
	}

	public void setEvent(CoreEvent event) {
		this.event = event;
	}

}
