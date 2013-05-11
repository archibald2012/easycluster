/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteArrayTLVDecoder implements TLVDecoder {

	@SuppressWarnings("unused")
	private static final Logger	logger	= LoggerFactory.getLogger(ByteArrayTLVDecoder.class);

	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		if (tlvLength == tlvValue.length) {
			return tlvValue;
		} else {
			return ArrayUtils.subarray(tlvValue, 0, tlvLength);
		}
	}

}
