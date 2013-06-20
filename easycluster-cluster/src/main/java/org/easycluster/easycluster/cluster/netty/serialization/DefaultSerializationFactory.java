package org.easycluster.easycluster.cluster.netty.serialization;

public class DefaultSerializationFactory implements SerializationFactory {

	private Serialization	serializable;

	public DefaultSerializationFactory(SerializationConfig config) {

		if (SerializeType.BINARY == config.getSerializeType()) {
			ByteBeanSerialization serializable = new ByteBeanSerialization();
			serializable.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.JSON == config.getSerializeType()) {
			JsonBeanSerialization serializable = new JsonBeanSerialization();
			serializable.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.TLV == config.getSerializeType()) {
			TlvBeanSerialization serializable = new TlvBeanSerialization();
			serializable.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			serializable.setDumpBytes(config.getDumpBytes());
			serializable.setEncryptKey(config.getEncryptKey());
			this.serializable = serializable;
		} else if (SerializeType.KV == config.getSerializeType()) {
			KvBeanSerialization serializable = new KvBeanSerialization();
			serializable.setDebugEnabled(config.isEncodeBytesDebugEnabled());
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
