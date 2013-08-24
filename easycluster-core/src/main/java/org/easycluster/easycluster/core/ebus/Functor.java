package org.easycluster.easycluster.core.ebus;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: Functor.java 211 2012-03-30 13:18:42Z archie $
 */
public class Functor implements Clojure {

	private static final Logger	LOGGER	= LoggerFactory.getLogger(Functor.class);

	private Object				target	= null;
	private Method				method	= null;

	public Functor(Object target, String methodName) {
		this.target = target;
		if (null == this.target) {
			throw new RuntimeException(" target is null.");
		}

		Method[] methods = null;
		Class<?> itr = target.getClass();
		while (!itr.equals(Object.class)) {
			methods = (Method[]) ArrayUtils.addAll(itr.getDeclaredMethods(), methods);
			itr = itr.getSuperclass();
		}
		for (Method methodItr : methods) {
			if (methodItr.getName().equals(methodName)) {
				methodItr.setAccessible(true);
				this.method = methodItr;
			}
		}
		if (null == this.method) {
			throw new RuntimeException("method [" + target.getClass() + "." + methodName + "] !NOT! exist.");
		}
	}

	public Functor(Object target, Method method) {
		this.target = target;
		if (null == this.target) {
			throw new RuntimeException(" target is null.");
		}
		this.method = method;
		if (null == this.method) {
			throw new RuntimeException(" method is null.");
		}
	}

	@Override
	public Object execute(Object... args) {
		try {
			return method.invoke(target, args);
		} catch (IllegalArgumentException e) {
			LOGGER.error("execute", e);
		} catch (IllegalAccessException e) {
			LOGGER.error("execute", e);
		} catch (InvocationTargetException e) {
			LOGGER.error("execute", e);
		}
		return null;
	}

	public Future<Object> execute(ExecutorService exec, final Object... args) {
		return exec.submit(new Callable<Object>() {

			public Object call() {
				try {
					return method.invoke(target, args);
				} catch (IllegalArgumentException e) {
					LOGGER.error("execute", e);
				} catch (IllegalAccessException e) {
					LOGGER.error("execute", e);
				} catch (InvocationTargetException e) {
					LOGGER.error("execute", e);
				}
				return null;
			}
		});

	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Functor other = (Functor) obj;
		if (target == null) {
			if (other.target != null)
				return false;
		} else if (!method.equals(other.method))
			return false;
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();

		sb.append(this.target);
		sb.append(".");
		sb.append(this.method.getName());
		return sb.toString();
	}

}
