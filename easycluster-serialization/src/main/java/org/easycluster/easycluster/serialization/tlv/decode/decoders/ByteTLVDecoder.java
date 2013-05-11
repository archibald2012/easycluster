/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ByteTLVDecoder implements TLVDecoder {

	@SuppressWarnings("unused")
	private static final Logger	logger	= LoggerFactory.getLogger(ByteTLVDecoder.class);

	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return tlvValue[0];
	}

}
