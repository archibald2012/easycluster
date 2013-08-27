package org.easycluster.easycluster.cluster.netty.serialization;

import java.io.UnsupportedEncodingException;

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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanBinarySerialization implements Serialization {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(BeanBinarySerialization.class);

	private BeanFieldCodec		beanFieldCodec	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public <T> byte[] serialize(T object) {
		if (object instanceof byte[]) {
			return (byte[]) object;
		}

		byte[] bytes = getBeanFieldCodec().encode(getBeanFieldCodec().getEncContextFactory().createEncContext(object, object.getClass(), null));

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

		T object = (T) getBeanFieldCodec().decode(getBeanFieldCodec().getDecContextFactory().createDecContext(bytes, type, null, null)).getValue();

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Deserialize object raw bytes --> {}, deserialized object:{}", ByteUtil.bytesAsHexString(bytes, dumpBytes),
					ToStringBuilder.reflectionToString(object));
		}

		return object;
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
