package org.easycluster.easycluster.cluster.server;

public interface MessageClosure<Request, Response> {
	Response execute(Request message);
}
