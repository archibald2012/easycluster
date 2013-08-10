package org.easycluster.easycluster.cluster.manager.event.handler;

import java.util.List;

public interface ContextManager {

	<T> T getObject(String name, Class<T> clazz);

	<T> List<T> getObjectsByType(Class<T> clazz);
}
