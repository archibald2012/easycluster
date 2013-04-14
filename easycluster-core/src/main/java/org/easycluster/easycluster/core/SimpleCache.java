package org.easycluster.easycluster.core;

import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

public class SimpleCache<K, V> {
	private ConcurrentHashMap<K, V> map = new ConcurrentHashMap<K, V>();

	public V get(K key, Callable<V> ifAbsent) {
		V value = map.get(key);
		if (value == null) {
			try {
				value = ifAbsent.call();
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			map.putIfAbsent(key, value);
		}
		return value;
	}
}
