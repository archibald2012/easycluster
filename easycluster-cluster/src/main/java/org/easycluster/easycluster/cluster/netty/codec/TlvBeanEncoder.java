package org.easycluster.easycluster.cluster.netty.codec;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncoderRepository;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoderOfBean;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BeanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BooleanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteArrayTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.IntTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.LongTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ShortTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.StringTLVEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TlvBeanEncoder implements Transformer<XipSignal, byte[]> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(TlvBeanEncoder.class);

	private TLVEncoderOfBean	tlvBeanEncoder	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public byte[] transform(XipSignal signal) {
		SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
		if (null == attr) {
			throw new InvalidMessageException("invalid signal, no messageCode defined.");
		}

		byte[] messageCodeBytes = getTlvBeanEncoder().getEncodeContextFactory().createEncodeContext(Integer.class, null)
				.getNumberCodec().int2Bytes(attr.messageCode(), 4);
		
		byte[] bodyBytes = ByteUtil.union(getTlvBeanEncoder().encode(signal,
				getTlvBeanEncoder().getEncodeContextFactory().createEncodeContext(signal.getClass(), null)));

		byte[] bytes = ArrayUtils.addAll(messageCodeBytes, bodyBytes);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and signal raw bytes --> {}", ToStringBuilder.reflectionToString(signal),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		if (bytes.length > 0 && encryptKey != null) {
			try {
				bytes = DES.encryptThreeDESECB(bytes, encryptKey);

				if (LOGGER.isDebugEnabled() && isDebugEnabled) {
					LOGGER.debug("After encryption, signal raw bytes --> {}", ByteUtil.bytesAsHexString(bytes, dumpBytes));
				}
			} catch (Exception e) {
				String error = "Failed to encrypt the body due to error " + e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		return bytes;
	}

	public TLVEncoderOfBean getTlvBeanEncoder() {
		if (tlvBeanEncoder == null) {
			DefaultEncoderRepository encoderRepository = new DefaultEncoderRepository();
			encoderRepository.add(byte[].class, new ByteArrayTLVEncoder());
			encoderRepository.add(int.class, new IntTLVEncoder());
			encoderRepository.add(Integer.class, new IntTLVEncoder());
			encoderRepository.add(byte.class, new ByteTLVEncoder());
			encoderRepository.add(Byte.class, new ByteTLVEncoder());
			encoderRepository.add(short.class, new ShortTLVEncoder());
			encoderRepository.add(Short.class, new ShortTLVEncoder());
			encoderRepository.add(long.class, new LongTLVEncoder());
			encoderRepository.add(Long.class, new LongTLVEncoder());
			encoderRepository.add(boolean.class, new BooleanTLVEncoder());
			encoderRepository.add(Boolean.class, new BooleanTLVEncoder());
			encoderRepository.add(String.class, new StringTLVEncoder());

			DefaultEncodeContextFactory encodeContextFactory = new DefaultEncodeContextFactory();
			encodeContextFactory.setEncoderRepository(encoderRepository);
			encodeContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			BeanTLVEncoder beanEncoder = new BeanTLVEncoder();
			beanEncoder.setEncodeContextFactory(encodeContextFactory);

			encoderRepository.add(Object.class, beanEncoder);

			this.tlvBeanEncoder = beanEncoder;
		}
		return tlvBeanEncoder;
	}

	public void setTlvBeanEncoder(TLVEncoderOfBean tlvBeanEncoder) {
		this.tlvBeanEncoder = tlvBeanEncoder;
	}

	public int getDumpBytes() {
		return dumpBytes;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
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
