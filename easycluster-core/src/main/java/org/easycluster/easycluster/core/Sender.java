package org.easycluster.easycluster.core;

public interface Sender {

	void send(Object bean);

	void send(Object object, Closure closure);
}
