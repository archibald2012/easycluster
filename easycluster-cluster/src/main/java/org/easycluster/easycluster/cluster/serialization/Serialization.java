package org.easycluster.easycluster.cluster.serialization;

public interface Serialization {

	<T> byte[] serialize(T object);

	<T> T deserialize(byte[] bytes, Class<T> clazz);
}
