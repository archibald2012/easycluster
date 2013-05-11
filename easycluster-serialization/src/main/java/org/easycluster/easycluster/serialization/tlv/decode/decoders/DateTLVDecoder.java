/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import java.util.Date;

import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DateTLVDecoder implements TLVDecoder {

	@SuppressWarnings("unused")
	private static final Logger	logger	= LoggerFactory.getLogger(DateTLVDecoder.class);

	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return new Date(ctx.getNumberCodec().bytes2Long(tlvValue, tlvLength));
	}

}
