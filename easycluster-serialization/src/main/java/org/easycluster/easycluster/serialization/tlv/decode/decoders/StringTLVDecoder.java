/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StringTLVDecoder implements TLVDecoder {

	private static final Logger	logger	= LoggerFactory.getLogger(StringTLVDecoder.class);

	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
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
			return new String(tlvValue, 0, tlvLength, charset);
		} catch (UnsupportedEncodingException e) {
			logger.error("StringTLVDecoder:", e);
		}

		return null;
	}

}
