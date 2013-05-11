/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LongTLVDecoder implements TLVDecoder {

	@SuppressWarnings("unused")
	private static final Logger	logger	= LoggerFactory.getLogger(LongTLVDecoder.class);

	@Override
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return ctx.getNumberCodec().bytes2Long(tlvValue, tlvLength);
	}

}
