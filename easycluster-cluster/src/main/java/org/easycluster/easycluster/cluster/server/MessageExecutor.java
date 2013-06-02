package org.easycluster.easycluster.cluster.server;

import org.easycluster.easycluster.core.Closure;

public interface MessageExecutor {

	void execute(Object message, Closure closure);

	void shutdown();
}
