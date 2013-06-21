package org.easycluster.easycluster.cluster.server;

import java.util.ArrayList;
import java.util.List;

import org.easycluster.easycluster.core.Closure;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;

public class PartitionedThreadPoolMessageExecutor implements MessageExecutor {

	private final List<MessageExecutor>	messageExecutors	= new ArrayList<MessageExecutor>();

	public PartitionedThreadPoolMessageExecutor(MessageClosureRegistry messageHandlerRegistry, int corePoolSize, int maxPoolSize, int keepAliveTime,
			int partitionNum) {
		for (int i = 0; i < partitionNum; i++) {
			ThreadPoolMessageExecutor executor = new ThreadPoolMessageExecutor("threadpool-message-executor-" + i, corePoolSize, maxPoolSize, keepAliveTime,
					messageHandlerRegistry);
			messageExecutors.add(executor);
		}
	}

	@Override
	public void execute(Object message, Closure closure) {

		int index;
		if (message instanceof AbstractXipSignal) {
			long client = Math.abs(((AbstractXipSignal) message).getClient());
			index = (int) (client % messageExecutors.size());
		} else {
			index = message.hashCode() % messageExecutors.size();
		}

		MessageExecutor executor = messageExecutors.get(index);

		executor.execute(message, closure);
	}

	@Override
	public void shutdown() {
		for (MessageExecutor messageExecutor : messageExecutors) {
			messageExecutor.shutdown();
		}

	}

}
