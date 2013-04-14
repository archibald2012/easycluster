package org.easycluster.easycluster.cluster.common;

import java.lang.reflect.Method;

import org.apache.commons.lang.ArrayUtils;

public class ClassUtil {

	public static Method[] getAllMethodOf(final Class<?> clazz) {
		Method[] methods = null;

		Class<?> itr = clazz;
		while (!itr.equals(Object.class)) {
			methods = (Method[]) ArrayUtils.addAll(itr.getDeclaredMethods(),
					methods);
			itr = itr.getSuperclass();
		}

		return methods;
	}

}
