
package org.easycluster.easycluster.serialization.bytebean.field;

import java.lang.reflect.Field;
import java.util.Comparator;

/**
 * 字段的编解码描述
 * 
 * @author Archibald.Wang
 * @version $Id: ByteFieldDesc.java 14 2012-01-10 11:54:14Z archie $
 */
public interface ByteFieldDesc {
	int getIndex();
	int getByteSize();
	Field getField();
	Class<?> getFieldType();
	boolean hasLength();
	int getLength(Object owner);
	int getStringLengthInBytes(Object owner);
	String getCharset();
	String getDescription();
	int getFixedLength();

	static final Comparator<ByteFieldDesc> comparator = new Comparator<ByteFieldDesc>() {
		public int compare(ByteFieldDesc desc1, ByteFieldDesc desc2) {
			int ret = desc1.getIndex() - desc2.getIndex();
			if (0 == ret) {
				throw new RuntimeException("field1:" + desc1.getField()
						+ "/field2:" + desc2.getField()
						+ " has the same index value, internal error.");
			}
			return ret;
		}
	};
}
