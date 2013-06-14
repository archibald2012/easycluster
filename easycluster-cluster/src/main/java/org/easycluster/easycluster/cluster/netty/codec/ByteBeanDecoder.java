package org.easycluster.easycluster.cluster.netty.codec;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.core.Transformer;
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
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteBeanDecoder implements Transformer<byte[], XipSignal> {

	private static final Logger		LOGGER			= LoggerFactory.getLogger(ByteBeanDecoder.class);

	private BeanFieldCodec			beanFieldCodec	= null;
	private MsgCode2TypeMetainfo	typeMetaInfo	= null;
	private int						dumpBytes		= 256;
	private boolean					isDebugEnabled	= true;
	private byte[]					encryptKey		= null;

	@Override
	public XipSignal transform(byte[] source) {

		if (source.length > 0 && encryptKey != null) {
			try {
				source = DES.decryptThreeDESECB(source, encryptKey);
			} catch (Exception e) {
				String error = "Failed to decrypt the bytes due to error " + e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Signal raw bytes --> {}", ByteUtil.bytesAsHexString(source, dumpBytes));
		}
		
		byte[] headerBytes = ArrayUtils.subarray(source, 0, 4);

		int messageCode = getBeanFieldCodec().getDecContextFactory().createDecContext(headerBytes, Integer.class, null, null).getNumberCodec()
				.bytes2Int(headerBytes, headerBytes.length);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("message code bytes --> {}, decoded messageCode --> {}", ByteUtil.bytesAsHexString(headerBytes, dumpBytes), messageCode);
		}

		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknown message code:" + messageCode);
		}

		byte[] bodyBytes = ArrayUtils.subarray(source, 4, source.length);

		XipSignal signal = (XipSignal) getBeanFieldCodec().decode(getBeanFieldCodec().getDecContextFactory().createDecContext(bodyBytes, type, null, null))
				.getValue();

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("body bytes --> {}, decoded signal:{}", ByteUtil.bytesAsHexString(bodyBytes, dumpBytes), ToStringBuilder.reflectionToString(signal));
		}

		return signal;

	}

	public void setBeanFieldCodec(BeanFieldCodec beanFieldCodec) {
		this.beanFieldCodec = beanFieldCodec;
	}

	public BeanFieldCodec getBeanFieldCodec() {
		if (beanFieldCodec == null) {
			DefaultCodecProvider codecProvider = new DefaultCodecProvider();

			codecProvider.addCodec(new AnyCodec()).addCodec(new ByteCodec()).addCodec(new ShortCodec()).addCodec(new IntCodec()).addCodec(new LongCodec())
					.addCodec(new BooleanCodec()).addCodec(new FloatCodec()).addCodec(new DoubleCodec()).addCodec(new LenStringCodec())
					.addCodec(new LenByteArrayCodec()).addCodec(new LenListCodec()).addCodec(new LenArrayCodec());

			EarlyStopBeanCodec beanCodec = new EarlyStopBeanCodec(new DefaultField2Desc());
			codecProvider.addCodec(beanCodec);

			DefaultEncContextFactory encContextFactory = new DefaultEncContextFactory();
			DefaultDecContextFactory decContextFactory = new DefaultDecContextFactory();

			encContextFactory.setCodecProvider(codecProvider);
			encContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			decContextFactory.setCodecProvider(codecProvider);
			decContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			beanCodec.setDecContextFactory(decContextFactory);
			beanCodec.setEncContextFactory(encContextFactory);

			this.beanFieldCodec = beanCodec;
		}
		return beanFieldCodec;
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
		if (encryptKey != null) {
			this.encryptKey = encryptKey.getBytes();
		}
	}
}
