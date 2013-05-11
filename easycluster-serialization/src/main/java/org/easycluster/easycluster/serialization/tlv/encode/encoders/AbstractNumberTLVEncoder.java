package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;

public class AbstractNumberTLVEncoder {
	protected static int getAnnotationByteSize(TLVEncodeContext ctx) {
		Field field = ctx.getValueField();
		if (null != field) {
			TLVAttribute attr = field.getAnnotation(TLVAttribute.class);
			if (null != attr) {
				return attr.bytes();
			}
		}

		return -1;
	}
}
