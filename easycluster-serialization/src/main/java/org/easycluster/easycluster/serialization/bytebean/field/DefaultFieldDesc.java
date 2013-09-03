
package org.easycluster.easycluster.serialization.bytebean.field;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.ByteBeanUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * 字段的编解码描述
 * 
 * @author wangqi
 * @version $Id: DefaultFieldDesc.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultFieldDesc extends ByteBeanUtil implements ByteFieldDesc {

	private static final Logger LOGGER = LoggerFactory.getLogger(DefaultFieldDesc.class);
	
	private Field field;
	private int maxByteSize = -1;
	private int index;
	private int byteSize = -1;
	private Field lengthField = null;
	private String charset;
	private int bytesPerChar = 1;
	private String description;
	private int fixedLength = -1;

	/**
	 * @param field
	 *            the field to set
	 */
	public DefaultFieldDesc setField(Field field) {
		this.field = field;
		maxByteSize = super.type2DefaultByteSize(this.field.getType());
		return this;
	}

	/**
	 * @param index
	 *            the index to set
	 */
	public DefaultFieldDesc setIndex(int index) {
		this.index = index;
		return this;
	}

	/**
	 * @param byteSize
	 *            the byteSize to set
	 */
	public DefaultFieldDesc setByteSize(int byteSize) {
		this.byteSize = byteSize;
		return this;
	}

	/**
	 * @param lengthField
	 *            the lengthField to set
	 */
	public DefaultFieldDesc setLengthField(Field lengthField) {
		this.lengthField = lengthField;
		return this;
	}

	/**
	 * @param charset
	 *            the charset to set
	 */
	public DefaultFieldDesc setCharset(String charset) {
		this.charset = charset;
		if (charset.startsWith("UTF-16")) {
			bytesPerChar = 2;
		} else {
			bytesPerChar = 1;
		}
		return this;
	}

	public DefaultFieldDesc setFixedLength(int fixedLength) {
		this.fixedLength = fixedLength;
		return this;
	}

	public DefaultFieldDesc setDescription(String description) {
		this.description = description;
		return this;
	}

	@Override
	public int getIndex() {
		return this.index;
	}

	@Override
	public int getByteSize() {
		return -1 == byteSize ? maxByteSize : Math.min(byteSize, maxByteSize);
	}

	@Override
	public Field getField() {
		return field;
	}

	@Override
	public Class<?> getFieldType() {
		return field.getType();
	}

	@Override
	public boolean hasLength() {
		return null != lengthField;
	}

	@Override
	public int getLength(Object owner) {
		if (null == owner) {
			return -1;
		}
		if (null == lengthField) {
			return -1;
		}
		lengthField.setAccessible(true);
		try {
			Object value = lengthField.get(owner);
			if (null == value) {
				return -1;
			}
			if (value instanceof Long) {
				return ((Long) value).intValue();
			} else if (value instanceof Integer) {
				return ((Integer) value).intValue();
			} else if (value instanceof Short) {
				return ((Short) value).intValue();
			} else if (value instanceof Byte) {
				return ((Byte) value).intValue();
			} else {
				return -1;
			}
		} catch (IllegalArgumentException e) {
			LOGGER.error("", e);
			return -1;
		} catch (IllegalAccessException e) {
			LOGGER.error("", e);
			return -1;
		}
	}

	@Override
	public int getStringLengthInBytes(Object owner) {
		return getLength(owner) * this.bytesPerChar;
	}

	@Override
	public String getCharset() {
		return charset;
	}

	@Override
	public String getDescription() {
		return description;
	}

	@Override
	public int getFixedLength() {
		return this.fixedLength;
	}

}
