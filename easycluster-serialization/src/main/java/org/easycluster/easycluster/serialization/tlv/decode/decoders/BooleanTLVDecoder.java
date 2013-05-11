package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;

public class BooleanTLVDecoder implements TLVDecoder {

	@Override
	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		return new Boolean(0 != tlvValue[0]);
	}
}
