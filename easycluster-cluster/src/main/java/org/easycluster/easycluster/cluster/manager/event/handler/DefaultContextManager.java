package org.easycluster.easycluster.cluster.manager.event.handler;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.springframework.context.ApplicationContext;
import org.springframework.context.ApplicationContextAware;

public class DefaultContextManager implements ContextManager, ApplicationContextAware {

	private ApplicationContext	applicationContext;

	@Override
	public <T> T getObject(String name, Class<T> clazz) {
		return applicationContext.containsBean(name) ? (T) applicationContext.getBean(name, clazz) : null;
	}

	@Override
	public <T> List<T> getObjectsByType(Class<T> clazz) {
		List<T> foundList = new ArrayList<T>();
		Collection<T> objects = applicationContext.getBeansOfType(clazz).values();
		foundList.addAll(objects);
		return foundList;
	}

	public void setApplicationContext(ApplicationContext applicationContext) {
		this.applicationContext = applicationContext;
	}

}
