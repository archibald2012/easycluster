package org.easycluster.easycluster.cluster.netty.tcp;

import org.easycluster.easycluster.cluster.netty.codec.ByteBeanDecoder;
import org.easycluster.easycluster.cluster.netty.codec.ByteBeanEncoder;
import org.easycluster.easycluster.cluster.netty.codec.ProtocolCodecConfig;
import org.easycluster.easycluster.cluster.netty.codec.JsonBeanDecoder;
import org.easycluster.easycluster.cluster.netty.codec.JsonBeanEncoder;
import org.easycluster.easycluster.cluster.netty.codec.ProtocolCodecFactory;
import org.easycluster.easycluster.cluster.netty.codec.SerializeType;
import org.jboss.netty.channel.ChannelDownstreamHandler;
import org.jboss.netty.channel.ChannelUpstreamHandler;

public class DefaultProtocolCodecFactory implements ProtocolCodecFactory {

	private ChannelDownstreamHandler	encoder;
	private ChannelUpstreamHandler		decoder;

	public DefaultProtocolCodecFactory(ProtocolCodecConfig config) {

		NettyBeanEncoder encoder = new NettyBeanEncoder();
		NettyBeanDecoder decoder = new NettyBeanDecoder(config.getMaxContentLength(), config.getLengthFieldOffset(), config.getLengthFieldLength(), 0, 0);

		if (SerializeType.BINARY == config.getSerializeType()) {
			ByteBeanEncoder bytesEncoder = new ByteBeanEncoder();
			bytesEncoder.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			bytesEncoder.setDumpBytes(config.getDumpBytes());
			bytesEncoder.setEncryptKey(config.getEncryptKey());

			encoder.setBytesEncoder(bytesEncoder);

			ByteBeanDecoder bytesDecoder = new ByteBeanDecoder();
			bytesDecoder.setDebugEnabled(config.isDecodeBytesDebugEnabled());
			bytesDecoder.setDumpBytes(config.getDumpBytes());
			bytesDecoder.setEncryptKey(config.getEncryptKey());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			decoder.setByteDecoder(bytesDecoder);

		} else if (SerializeType.JSON == config.getSerializeType()) {
			JsonBeanEncoder bytesEncoder = new JsonBeanEncoder();
			bytesEncoder.setDebugEnabled(config.isEncodeBytesDebugEnabled());
			bytesEncoder.setDumpBytes(config.getDumpBytes());
			bytesEncoder.setEncryptKey(config.getEncryptKey());

			encoder.setBytesEncoder(bytesEncoder);

			JsonBeanDecoder bytesDecoder = new JsonBeanDecoder();
			bytesDecoder.setDebugEnabled(config.isDecodeBytesDebugEnabled());
			bytesDecoder.setDumpBytes(config.getDumpBytes());
			bytesDecoder.setEncryptKey(config.getEncryptKey());
			bytesDecoder.setTypeMetaInfo(config.getTypeMetaInfo());
			decoder.setByteDecoder(bytesDecoder);
		} else {
			throw new UnsupportedOperationException("Serialize type " + config.getSerializeType() + " not supported yet.");
		}

		this.encoder = encoder;

		this.decoder = decoder;
	}

	@Override
	public ChannelDownstreamHandler getEncoder() {
		return encoder;
	}

	@Override
	public ChannelUpstreamHandler getDecoder() {
		return decoder;
	}

}
