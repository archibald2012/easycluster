package org.easycluster.easycluster.cluster.serialization;

public class DefaultSerializationFactory implements SerializationFactory {

	private Serialization	serializable;

	public DefaultSerializationFactory(SerializationConfig config) {

		if (SerializeType.BYTE_BEAN == config.getSerializeType()) {
			BeanBinarySerialization serializable = new BeanBinarySerialization();
			serializable.setDebugEnabled(config.isSerializeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.JSON == config.getSerializeType()) {
			BeanJsonSerialization serializable = new BeanJsonSerialization();
			serializable.setDebugEnabled(config.isSerializeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.TLV == config.getSerializeType()) {
			BeanTlvSerialization serializable = new BeanTlvSerialization();
			serializable.setDebugEnabled(config.isSerializeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.KV == config.getSerializeType()) {
			BeanKvSerialization serializable = new BeanKvSerialization();
			serializable.setDebugEnabled(config.isSerializeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.JAVA == config.getSerializeType()) {
			BeanJavaSerialization serializable = new BeanJavaSerialization();
			serializable.setDebugEnabled(config.isSerializeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else {
			throw new UnsupportedOperationException("Serialize type " + config.getSerializeType() + " not supported yet.");
		}
	}

	@Override
	public Serialization getSerialization() {
		return serializable;
	}

}
