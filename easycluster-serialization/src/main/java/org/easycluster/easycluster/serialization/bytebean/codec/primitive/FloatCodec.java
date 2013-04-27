
package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: FloatCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class FloatCodec extends AbstractPrimitiveCodec implements
		ByteFieldCodec {

	private static final Logger logger = LoggerFactory
			.getLogger(FloatCodec.class);

	@Override
	public Class<?>[] getFieldType() {
		return new Class<?>[] { float.class, Float.class };
	}

	@Override
	public DecResult decode(DecContext ctx) {
		byte[] bytes = ctx.getDecBytes();
		int byteLength = ctx.getByteSize();
		NumberCodec numberCodec = ctx.getNumberCodec();

		if (byteLength > bytes.length) {
			String errmsg = "FloatCodec: not enough bytes for decode, need ["
					+ byteLength + "], actually [" + bytes.length + "].";
			if (null != ctx.getField()) {
				errmsg += "/ cause field is [" + ctx.getField() + "]";
			}
			logger.error(errmsg);
			throw new RuntimeException(errmsg);
		}

		return new DecResult(numberCodec.bytes2Float(bytes, byteLength),
				ArrayUtils.subarray(bytes, byteLength, bytes.length));
	}

	@Override
	public byte[] encode(EncContext ctx) {
		float enc = ((Float) ctx.getEncObject()).floatValue();
		int byteLength = ctx.getByteSize();
		NumberCodec numberCodec = ctx.getNumberCodec();

		return numberCodec.float2Bytes(enc, byteLength);
	}

}