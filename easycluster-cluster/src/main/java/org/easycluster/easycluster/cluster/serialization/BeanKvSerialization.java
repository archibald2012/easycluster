package org.easycluster.easycluster.cluster.serialization;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.serialization.kv.codec.DefaultKVCodec;
import org.easycluster.easycluster.serialization.kv.codec.KVCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanKvSerialization implements Serialization {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(BeanKvSerialization.class);

	private static final String	ENCODING		= "UTF-8";

	private KVCodec				kvCodec			= new DefaultKVCodec();
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public <T> byte[] serialize(T object) {
		if (object instanceof byte[]) {
			return (byte[]) object;
		}

		String text = kvCodec.encode(kvCodec.getEncContextFactory().createEncContext(object, object.getClass()));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Serialize object {}, and object as KV --> {}", ToStringBuilder.reflectionToString(object), text);
		}

		byte[] bytes = null;
		try {
			bytes = text.getBytes(ENCODING);
		} catch (UnsupportedEncodingException ignore) {
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Serialize object {}, and object raw bytes --> {}", ToStringBuilder.reflectionToString(object),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		if (bytes != null && bytes.length > 0 && encryptKey != null) {
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

		String text = null;
		try {
			text = new String(bytes, ENCODING);
		} catch (UnsupportedEncodingException ingore) {
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("After decryption, and object as KV --> {}", text);
		}

		T object = (T) kvCodec.decode(kvCodec.getDecContextFactory().createDecContext(text, type, null));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Deserialize object raw bytes --> {}, deserialized object:{}", ByteUtil.bytesAsHexString(bytes, dumpBytes),
					ToStringBuilder.reflectionToString(object));
		}

		return object;
	}

	public void setKvCodec(KVCodec kvCodec) {
		this.kvCodec = kvCodec;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
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
