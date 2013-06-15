package org.easycluster.easycluster.cluster.netty.codec;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecoderRepository;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoderOfBean;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BeanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BooleanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteArrayTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.IntTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.LongTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ShortTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.StringTLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlvBeanDecoder implements Transformer<byte[], XipSignal> {

	private static final Logger		LOGGER			= LoggerFactory.getLogger(TlvBeanDecoder.class);

	private TLVDecoderOfBean		tlvBeanDecoder	= null;
	private Int2TypeMetainfo	typeMetaInfo	= null;
	private int						dumpBytes		= 256;
	private boolean					isDebugEnabled	= false;
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

		int messageCode = getTlvBeanDecoder().getDecodeContextFactory().createDecodeContext(Integer.class, null).getNumberCodec()
				.bytes2Int(headerBytes, headerBytes.length);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("message code bytes --> {}, decoded messageCode --> {}", ByteUtil.bytesAsHexString(headerBytes, dumpBytes), messageCode);
		}

		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknown message code:" + messageCode);
		}

		byte[] bodyBytes = ArrayUtils.subarray(source, 4, source.length);

		XipSignal signal = (XipSignal) getTlvBeanDecoder().decode(bodyBytes.length, bodyBytes,
				getTlvBeanDecoder().getDecodeContextFactory().createDecodeContext(type, null));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("body bytes --> {}, decoded signal:{}", ByteUtil.bytesAsHexString(bodyBytes, dumpBytes), ToStringBuilder.reflectionToString(signal));
		}

		return signal;

	}

	public TLVDecoderOfBean getTlvBeanDecoder() {
		if (tlvBeanDecoder == null) {
			DefaultDecoderRepository decoderRepository = new DefaultDecoderRepository();
			decoderRepository.add(byte[].class, new ByteArrayTLVDecoder());
			decoderRepository.add(int.class, new IntTLVDecoder());
			decoderRepository.add(Integer.class, new IntTLVDecoder());
			decoderRepository.add(byte.class, new ByteTLVDecoder());
			decoderRepository.add(Byte.class, new ByteTLVDecoder());
			decoderRepository.add(short.class, new ShortTLVDecoder());
			decoderRepository.add(Short.class, new ShortTLVDecoder());
			decoderRepository.add(long.class, new LongTLVDecoder());
			decoderRepository.add(Long.class, new LongTLVDecoder());
			decoderRepository.add(boolean.class, new BooleanTLVDecoder());
			decoderRepository.add(Boolean.class, new BooleanTLVDecoder());
			decoderRepository.add(String.class, new StringTLVDecoder());

			DefaultDecodeContextFactory decodeContextFactory = new DefaultDecodeContextFactory();
			decodeContextFactory.setDecoderRepository(decoderRepository);
			decodeContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			BeanTLVDecoder beanDecoder = new BeanTLVDecoder();
			beanDecoder.setDecodeContextFactory(decodeContextFactory);

			decoderRepository.add(Object.class, beanDecoder);

			this.tlvBeanDecoder = beanDecoder;
		}
		return tlvBeanDecoder;
	}

	public void setTlvBeanDecoder(TLVDecoderOfBean tlvBeanDecoder) {
		this.tlvBeanDecoder = tlvBeanDecoder;
	}

	public void setTypeMetaInfo(Int2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public Int2TypeMetainfo getTypeMetaInfo() {
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
