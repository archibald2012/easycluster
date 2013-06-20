package org.easycluster.easycluster.cluster.netty.serialization;

public interface Serialization {

	<T> byte[] serialize(T signal);

	<T> T deserialize(byte[] bytes, Class<T> clazz);
}
