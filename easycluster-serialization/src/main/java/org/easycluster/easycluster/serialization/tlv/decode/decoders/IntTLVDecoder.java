/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;

public class IntTLVDecoder implements TLVDecoder {

	@Override
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return ctx.getNumberCodec().bytes2Int(tlvValue, tlvLength);
	}

}
