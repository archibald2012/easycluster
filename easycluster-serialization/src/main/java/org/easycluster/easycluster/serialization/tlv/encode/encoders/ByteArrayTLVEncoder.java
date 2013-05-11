/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.util.Arrays;
import java.util.List;

import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoder;

public class ByteArrayTLVEncoder implements TLVEncoder {

	public List<byte[]> encode(Object src, TLVEncodeContext ctx) {
		return Arrays.asList((byte[]) src);
	}

}
