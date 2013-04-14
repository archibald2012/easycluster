package org.easycluster.easycluster.core;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.concurrent.Executor;

import org.apache.commons.lang.ArrayUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: Functor.java 211 2012-03-30 13:18:42Z archie $
 */
public class Functor implements Closure {

	private static final Logger logger = LoggerFactory.getLogger(Functor.class);

	private Object target = null;
	private Method method = null;
	private boolean canceled = false;

	public Functor(Object target, String methodName) {
		this.target = target;
		if (null == this.target) {
			throw new RuntimeException(" target is null.");
		}

		Method[] methods = null;
		Class<?> itr = target.getClass();
		while (!itr.equals(Object.class)) {
			methods = (Method[]) ArrayUtils.addAll(itr.getDeclaredMethods(),
					methods);
			itr = itr.getSuperclass();
		}
		for (Method methodItr : methods) {
			if (methodItr.getName().equals(methodName)) {
				methodItr.setAccessible(true);
				this.method = methodItr;
			}
		}
		if (null == this.method) {
			throw new RuntimeException("method [" + target.getClass() + "."
					+ methodName + "] !NOT! exist.");
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

	public Object execute(Object... args) {
		if (!canceled) {
			try {
				return method.invoke(target, args);
			} catch (IllegalArgumentException e) {
				logger.error("execute", e);
			} catch (IllegalAccessException e) {
				logger.error("execute", e);
			} catch (InvocationTargetException e) {
				logger.error("execute", e);
			}
		}
		return null;
	}

	public void execute(Executor exec, final Object... args) {
		if (!canceled) {
			exec.execute(new Runnable() {

				public void run() {
					try {
						method.invoke(target, args);
					} catch (Exception e) {
						logger.error("execute:", e);
					}
				}
			});
		}
	}

	public Object getTarget() {
		return target;
	}

	public Method getMethod() {
		return method;
	}

	/**
	 * @return the canceled
	 */
	public boolean isCanceled() {
		return canceled;
	}

	/**
	 * @param canceled
	 *            the canceled to set
	 */
	public void setCanceled(boolean canceled) {
		this.canceled = canceled;
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
		if (this.canceled) {
			sb.append("[canceled]");
		}
		return sb.toString();
	}

	@Override
	public void execute(Object msg) {
		execute(new Object[] { msg });
	}

}
