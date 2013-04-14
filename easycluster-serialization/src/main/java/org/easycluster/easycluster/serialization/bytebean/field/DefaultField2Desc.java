
package org.easycluster.easycluster.serialization.bytebean.field;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.annotation.ByteField;


/**
 * 
 * @author wangqi
 * @version $Id: DefaultField2Desc.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultField2Desc implements Field2Desc {

	@Override
	public ByteFieldDesc genDesc(Field field) {
		ByteField byteField = field.getAnnotation(ByteField.class);
		Class<?> clazz = field.getDeclaringClass();
		if (null != byteField) {
			try {
				DefaultFieldDesc desc = new DefaultFieldDesc()
						.setField(field)
						.setIndex(byteField.index())
						.setByteSize(byteField.bytes())
						.setCharset(byteField.charset())
						.setLengthField(
								byteField.length().equals("") ? null : clazz
										.getDeclaredField(byteField.length()))
						.setFixedLength(byteField.fixedLength());
				return desc;
			} catch (SecurityException e) {
				e.printStackTrace();
			} catch (NoSuchFieldException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

}
