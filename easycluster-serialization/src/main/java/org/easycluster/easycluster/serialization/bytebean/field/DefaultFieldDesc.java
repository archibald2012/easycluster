
package org.easycluster.easycluster.serialization.bytebean.field;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.ByteBeanUtil;


/**
 * 字段的编解码描述
 * 
 * @author wangqi
 * @version $Id: DefaultFieldDesc.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultFieldDesc extends ByteBeanUtil implements ByteFieldDesc {

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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getIndex
	 * ()
	 */
	@Override
	public int getIndex() {
		return this.index;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getByteSize
	 * ()
	 */
	@Override
	public int getByteSize() {
		return -1 == byteSize ? maxByteSize : Math.min(byteSize, maxByteSize);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getField
	 * ()
	 */
	@Override
	public Field getField() {
		return field;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getFieldType
	 * ()
	 */
	@Override
	public Class<?> getFieldType() {
		return field.getType();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#hasLength
	 * ()
	 */
	@Override
	public boolean hasLength() {
		return null != lengthField;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getLength
	 * (java.lang.Object)
	 */
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
			e.printStackTrace();
			return -1;
		} catch (IllegalAccessException e) {
			e.printStackTrace();
			return -1;
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#
	 * getStringLengthInBytes(java.lang.Object)
	 */
	@Override
	public int getStringLengthInBytes(Object owner) {
		return getLength(owner) * this.bytesPerChar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#getCharset
	 * ()
	 */
	@Override
	public String getCharset() {
		return charset;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#
	 * getDescription()
	 */
	@Override
	public String getDescription() {
		return description;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taotaosou.common.serialization.bytebean.field.ByteFieldDesc#
	 * getFixedLength()
	 */
	@Override
	public int getFixedLength() {
		return this.fixedLength;
	}

}
