
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
 * @version $Id: LenByteArrayCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class LenByteArrayCodec extends AbstractPrimitiveCodec implements
		ByteFieldCodec {

	private static final Logger logger = LoggerFactory
			.getLogger(LenByteArrayCodec.class);

	@Override
	public DecResult decode(DecContext ctx) {
		DecResult ret = ctx.getCodecOf(int.class).decode(
				ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(),
						int.class, ctx.getDecOwner(), null));
		int arrayLength = (Integer) ret.getValue();
		byte[] bytes = ret.getRemainBytes();

		if (bytes.length < arrayLength) {
			String errmsg = "ByteArrayCodec: not enough bytes for decode, need ["
					+ arrayLength + "], actually [" + bytes.length + "].";
			if (null != ctx.getField()) {
				errmsg += "/ cause field is [" + ctx.getField() + "]";
			}
			logger.error(errmsg);
			throw new RuntimeException(errmsg);
		}

		return new DecResult(
				(byte[]) ArrayUtils.subarray(bytes, 0, arrayLength),
				ArrayUtils.subarray(bytes, arrayLength, bytes.length));
	}

	@Override
	public byte[] encode(EncContext ctx) {
		byte[] array = (byte[]) ctx.getEncObject();

		return (byte[]) ArrayUtils.addAll(
				ctx.getCodecOf(int.class).encode(
						ctx.getEncContextFactory().createEncContext(
								(int) (null == array ? 0 : array.length),
								int.class, null)), array);
	}

	@Override
	public Class<?>[] getFieldType() {
		return new Class<?>[] { byte[].class };
	}

}
