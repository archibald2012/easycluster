
package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: BooleanCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class BooleanCodec extends AbstractPrimitiveCodec implements
		ByteFieldCodec {

	private static final Logger logger = LoggerFactory
			.getLogger(BooleanCodec.class);

	@Override
	public Class<?>[] getFieldType() {
		return new Class<?>[] { boolean.class, Boolean.class };
	}

	@Override
	public DecResult decode(DecContext ctx) {
		byte[] bytes = ctx.getDecBytes();
		if (bytes.length < 1) {
			String errmsg = "BooleanCodec: not enough bytes for decode, need [1], actually ["
					+ bytes.length + "].";
			if (null != ctx.getField()) {
				errmsg += "/ cause field is [" + ctx.getField() + "]";
			}
			logger.error(errmsg);
			throw new RuntimeException(errmsg);
		}
		return new DecResult((bytes[0] != 0), ArrayUtils.subarray(bytes, 1,
				bytes.length));
	}

	@Override
	public byte[] encode(EncContext ctx) {
		return ctx.getNumberCodec().short2Bytes(
				(Boolean) ctx.getEncObject() ? (short) 1 : (short) 0, 1);
	}

}
