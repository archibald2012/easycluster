package org.easycluster.easycluster.cluster.netty.serialization;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.exception.SerializationException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanJavaSerialization implements Serialization {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(BeanJavaSerialization.class);

	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public <T> byte[] serialize(T object) {

		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			ObjectOutputStream out = new ObjectOutputStream(outputStream);
			out.writeObject(object);
			out.close();
		} catch (IOException e) {
			String errorMsg = "Failed to serialize the object with error: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new SerializationException(errorMsg, e);
		}

		byte[] bytes = outputStream.toByteArray();

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
	public <T> T deserialize(byte[] bytes, Class<T> clazz) {
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

		T object = null;

		ByteArrayInputStream inputStream = new ByteArrayInputStream(bytes);

		try {
			ObjectInputStream in = new ObjectInputStream(inputStream);
			object = (T) in.readObject();
			in.close();
		} catch (IOException e) {
			String errorMsg = "Failed to deserialize the object with io error: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new SerializationException(errorMsg, e);
		} catch (ClassNotFoundException e) {
			String errorMsg = "Failed to deserialize the object with class not found error: " + e.getMessage();
			LOGGER.error(errorMsg);
			throw new SerializationException(errorMsg, e);
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("Deserialize object raw bytes --> {}, deserialized object:{}", ByteUtil.bytesAsHexString(bytes, dumpBytes),
					ToStringBuilder.reflectionToString(object));
		}

		return object;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
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
