package org.easycluster.easycluster.cluster.server;

import org.easymetrics.easymetrics.model.Measurable;

public interface MessageClosure<Request, Response> extends Measurable {
	Response execute(Request message);
}
