/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.util.Arrays;
import java.util.List;

import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class IntTLVEncoder extends AbstractNumberTLVEncoder implements TLVEncoder {

	@SuppressWarnings("unused")
	private static final Logger	logger				= LoggerFactory.getLogger(IntTLVEncoder.class);
	private static final int	DEFAULT_BYTE_SIZE	= 4;

	public List<byte[]> encode(Object from, TLVEncodeContext ctx) {
		byte[] ret = null;

		int byteSize = getAnnotationByteSize(ctx);
		if (-1 == byteSize) {
			byteSize = DEFAULT_BYTE_SIZE;
		}

		if (from instanceof Integer) {
			ret = ctx.getNumberCodec().int2Bytes(((Integer) from).intValue(), byteSize);
		} else if (from instanceof Long) {
			ret = ctx.getNumberCodec().long2Bytes(((Long) from).longValue(), byteSize);
		} else {
			throw new RuntimeException("IntTLVEncoder: wrong source type. [" + from.getClass() + "]");
		}

		return Arrays.asList(ret);
	}

}
