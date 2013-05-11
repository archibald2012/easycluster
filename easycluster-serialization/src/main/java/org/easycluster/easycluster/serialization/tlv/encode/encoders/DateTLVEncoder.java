/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoder;

public class DateTLVEncoder implements TLVEncoder {

	private static final int	BYTE_SIZE	= 8;

	public List<byte[]> encode(Object src, TLVEncodeContext ctx) {
		byte[] ret = null;

		if (src instanceof Date) {
			ret = ctx.getNumberCodec().long2Bytes(((Date) src).getTime(), BYTE_SIZE);
		} else {
			throw new RuntimeException("DateTLVEncoder: wrong source type. [" + src.getClass() + "]");
		}

		return Arrays.asList(ret);
	}

}
