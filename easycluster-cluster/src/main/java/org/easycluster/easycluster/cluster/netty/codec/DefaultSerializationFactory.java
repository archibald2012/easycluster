package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;

public class DefaultSerializationFactory implements SerializationFactory {

	private Transformer<XipSignal, byte[]>	encoder;
	private Transformer<byte[], XipSignal>	decoder;

	public DefaultSerializationFactory(SerializationConfig config) {

		if (SerializeType.BINARY == config.getSerializeType()) {
			ByteBeanEncoder bytesEncoder = new ByteBeanEncoder();
			bytesEncoder.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			bytesEncoder.setDumpBytes(config.getDumpBytes());
			bytesEncoder.setEncryptKey(config.getEncryptKey());
			this.encoder = bytesEncoder;

			ByteBeanDecoder bytesDecoder = new ByteBeanDecoder();
			bytesDecoder.setDebugEnabled(config.isDecodeBytesDebugEnabled());
			bytesDecoder.setDumpBytes(config.getDumpBytes());
			bytesDecoder.setEncryptKey(config.getEncryptKey());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			this.decoder = bytesDecoder;

		} else if (SerializeType.JSON == config.getSerializeType()) {
			JsonBeanEncoder bytesEncoder = new JsonBeanEncoder();
			bytesEncoder.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			bytesEncoder.setDumpBytes(config.getDumpBytes());
			bytesEncoder.setEncryptKey(config.getEncryptKey());
			this.encoder = bytesEncoder;

			JsonBeanDecoder bytesDecoder = new JsonBeanDecoder();
			bytesDecoder.setDebugEnabled(config.isDecodeBytesDebugEnabled());
			bytesDecoder.setDumpBytes(config.getDumpBytes());
			bytesDecoder.setEncryptKey(config.getEncryptKey());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			this.decoder = bytesDecoder;

		} else if (SerializeType.TLV == config.getSerializeType()) {
			TlvBeanEncoder bytesEncoder = new TlvBeanEncoder();
			bytesEncoder.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			bytesEncoder.setDumpBytes(config.getDumpBytes());
			bytesEncoder.setEncryptKey(config.getEncryptKey());
			this.encoder = bytesEncoder;

			TlvBeanDecoder bytesDecoder = new TlvBeanDecoder();
			bytesDecoder.setDebugEnabled(config.isDecodeBytesDebugEnabled());
			bytesDecoder.setDumpBytes(config.getDumpBytes());
			bytesDecoder.setEncryptKey(config.getEncryptKey());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			this.decoder = bytesDecoder;

		} else {
			throw new UnsupportedOperationException("Serialize type " + config.getSerializeType() + " not supported yet.");
		}
	}

	@Override
	public Transformer<XipSignal, byte[]> getEncoder() {
		return encoder;
	}

	@Override
	public Transformer<byte[], XipSignal> getDecoder() {
		return decoder;
	}

}
