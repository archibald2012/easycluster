package org.easycluster.easycluster.cluster.netty.serialization;

import java.io.UnsupportedEncodingException;
import java.util.Date;
import java.util.Map;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecoderRepository;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoderOfBean;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BeanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BooleanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteArrayTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.DateTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.IntTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.LongTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.MapTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ShortTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.StringTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncoderRepository;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoderOfBean;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BeanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BooleanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteArrayTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.DateTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.IntTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.LongTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.MapTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ShortTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.StringTLVEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanTlvSerialization implements Serialization {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(BeanTlvSerialization.class);

	private TLVEncoderOfBean	tlvBeanEncoder	= null;
	private TLVDecoderOfBean	tlvBeanDecoder	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public <T> byte[] serialize(T object) {
		if (object instanceof byte[]) {
			return (byte[]) object;
		}

		byte[] bytes = ByteUtil.union(getTlvBeanEncoder().encode(object,
				getTlvBeanEncoder().getEncodeContextFactory().createEncodeContext(object.getClass(), null)));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Serialize object {}, and object raw bytes --> {}", ToStringBuilder.reflectionToString(object),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		if (bytes.length > 0 && encryptKey != null) {
			try {
				bytes = DES.encryptThreeDESECB(bytes, encryptKey);

				if (LOGGER.isDebugEnabled() && isDebugEnabled) {
					LOGGER.debug("After encryption, object raw bytes --> {}", ByteUtil.bytesAsHexString(bytes, dumpBytes));
				}
			} catch (Exception e) {
				String error = "Failed to encrypt the body due to error " + e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		return bytes;
	}

	@SuppressWarnings("unchecked")
	@Override
	public <T> T deserialize(byte[] bytes, Class<T> type) {
		if (bytes.length > 0 && encryptKey != null) {
			try {
				if (LOGGER.isDebugEnabled() && isDebugEnabled) {
					LOGGER.debug("Before decryption, object raw bytes --> {}", ByteUtil.bytesAsHexString(bytes, dumpBytes));
				}
				bytes = DES.decryptThreeDESECB(bytes, encryptKey);
				if (LOGGER.isDebugEnabled() && isDebugEnabled) {
					LOGGER.debug("After decryption, object raw bytes --> {}", ByteUtil.bytesAsHexString(bytes, dumpBytes));
				}
			} catch (Exception e) {
				String error = "Failed to decrypt the bytes due to error " + e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		T object = (T) getTlvBeanDecoder().decode(bytes.length, bytes, getTlvBeanDecoder().getDecodeContextFactory().createDecodeContext(type, null));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Deserialize object raw bytes --> {}, deserialized object:{}", ByteUtil.bytesAsHexString(bytes, dumpBytes),
					ToStringBuilder.reflectionToString(object));
		}

		return object;
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
			encoderRepository.add(Date.class, new DateTLVEncoder());
			encoderRepository.add(Map.class, new MapTLVEncoder());

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
			decoderRepository.add(Date.class, new DateTLVDecoder());
			decoderRepository.add(Map.class, new MapTLVDecoder());

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
			try {
				this.encryptKey = encryptKey.getBytes("UTF-8");
			} catch (UnsupportedEncodingException ignore) {
			}
		}
	}
}
