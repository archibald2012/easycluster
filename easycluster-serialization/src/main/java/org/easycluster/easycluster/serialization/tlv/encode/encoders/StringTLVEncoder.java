/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.util.Arrays;
import java.util.List;

import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringTLVEncoder implements TLVEncoder {

	private static final Logger	logger	= LoggerFactory.getLogger(StringTLVEncoder.class);

	public List<byte[]> encode(Object from, TLVEncodeContext ctx) {
		String src = (String) from;
		String charset = "UTF-8";
		Field field = ctx.getValueField();
		if (null != field) {
			TLVAttribute attr = field.getAnnotation(TLVAttribute.class);
			if (null != attr) {
				if (!"".equals(attr.charset())) {
					charset = attr.charset();
				}
			}
		}
		try {
			return Arrays.asList(src.getBytes(charset));
		} catch (UnsupportedEncodingException e) {
			logger.error("StringTLVEncoder:", e);
		}
		return null;
	}
}
