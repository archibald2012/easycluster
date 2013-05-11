package org.easycluster.easycluster.cluster.netty.codec;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.serialization.bytebean.codec.AnyCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.array.LenArrayCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.array.LenListCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.bean.BeanFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.bean.EarlyStopBeanCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.BooleanCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.ByteCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.DoubleCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.FloatCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.IntCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LenByteArrayCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LenStringCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LongCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.ShortCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultDecContextFactory;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultEncContextFactory;
import org.easycluster.easycluster.serialization.bytebean.field.DefaultField2Desc;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipHeader;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.oneone.OneToOneDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class NettyBeanDecoder extends OneToOneDecoder {

	private static final Logger LOGGER = LoggerFactory
			.getLogger(NettyBeanDecoder.class);

	private BeanFieldCodec byteBeanCodec = null;
	private MsgCode2TypeMetainfo typeMetaInfo = null;

	private int dumpBytes = 256;
	private boolean isDebugEnabled = true;
	private byte[] encryptKey;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel,
			Object message) throws Exception {
		if (message instanceof ChannelBuffer) {
			ChannelBuffer content = (ChannelBuffer) message;
			if (!content.readable()) {
				return message;
			}
			return decodeXipSignal(content.array());
		}
		return message;
	}

	protected XipSignal decodeXipSignal(byte[] bytes) {

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug(ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		XipHeader header = (XipHeader) getByteBeanCodec().decode(
				getByteBeanCodec().getDecContextFactory().createDecContext(
						bytes, XipHeader.class, null, null)).getValue();

		Class<?> type = typeMetaInfo.find(header.getMessageCode());
		if (null == type) {
			throw new InvalidMessageException("Unknow message code:"
					+ header.getMessageCode());
		}
		if (header.getSequence() <= 0) {
			throw new InvalidMessageException("Invalid message sequence:"
					+ header.getSequence());
		}

		byte[] bodyBytes = ArrayUtils.subarray(bytes, XipHeader.HEADER_LENGTH,
				bytes.length);

		if (encryptKey != null) {
			try {
				bodyBytes = DES.decryptThreeDESECB(bodyBytes, encryptKey);
			} catch (Exception e) {
				String error = "Failed to decrypt the body due to error "
						+ e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		AbstractXipSignal signal = (AbstractXipSignal) getByteBeanCodec()
				.decode(getByteBeanCodec().getDecContextFactory()
						.createDecContext(bodyBytes, type, null, null))
				.getValue();

		if (null != signal) {
			signal.setIdentification(header.getSequence());
			signal.setClient(header.getClientId());
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("decoded signal:{}",
					ToStringBuilder.reflectionToString(signal));
		}

		return signal;
	}

	public void setByteBeanCodec(BeanFieldCodec byteBeanCodec) {
		this.byteBeanCodec = byteBeanCodec;
	}

	public BeanFieldCodec getByteBeanCodec() {
		if (byteBeanCodec == null) {
			DefaultCodecProvider codecProvider = new DefaultCodecProvider();

			codecProvider.addCodec(new AnyCodec()).addCodec(new ByteCodec())
					.addCodec(new ShortCodec()).addCodec(new IntCodec())
					.addCodec(new LongCodec()).addCodec(new BooleanCodec())
					.addCodec(new FloatCodec()).addCodec(new DoubleCodec())
					.addCodec(new LenStringCodec())
					.addCodec(new LenByteArrayCodec())
					.addCodec(new LenListCodec()).addCodec(new LenArrayCodec());

			EarlyStopBeanCodec byteBeanCodec = new EarlyStopBeanCodec(
					new DefaultField2Desc());
			codecProvider.addCodec(byteBeanCodec);

			DefaultEncContextFactory encContextFactory = new DefaultEncContextFactory();
			DefaultDecContextFactory decContextFactory = new DefaultDecContextFactory();

			encContextFactory.setCodecProvider(codecProvider);
			encContextFactory.setNumberCodec(DefaultNumberCodecs
					.getBigEndianNumberCodec());

			decContextFactory.setCodecProvider(codecProvider);
			decContextFactory.setNumberCodec(DefaultNumberCodecs
					.getBigEndianNumberCodec());

			byteBeanCodec.setDecContextFactory(decContextFactory);
			byteBeanCodec.setEncContextFactory(encContextFactory);

			this.byteBeanCodec = byteBeanCodec;
		}
		return byteBeanCodec;
	}

	public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public MsgCode2TypeMetainfo getTypeMetaInfo() {
		return typeMetaInfo;
	}

	public int getDumpBytes() {
		return dumpBytes;
	}

	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	public void setEncryptKey(String encryptKey) {
		this.encryptKey = encryptKey.getBytes();
	}

	public void setEncryptKey(byte[] encryptKey) {
		this.encryptKey = encryptKey;
	}
}
