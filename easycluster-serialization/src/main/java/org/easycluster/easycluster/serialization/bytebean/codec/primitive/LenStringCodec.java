package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class LenStringCodec extends AbstractPrimitiveCodec implements ByteFieldCodec {

	private static final Logger	logger					= LoggerFactory.getLogger(LenStringCodec.class);
	private static final String	XIP_STR_CHARSET	= "UTF-8";

	@Override
	public Class<?>[] getFieldType() {
		return new Class<?>[] { String.class };
	}

	@Override
	public DecResult decode(DecContext ctx) {
		DecResult ret = ctx.getCodecOf(short.class).decode(
				ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(), short.class, ctx.getDecOwner(), null));
		short arrayLength = (Short) ret.getValue();
		byte[] bytes = ret.getRemainBytes();

		if (bytes.length < arrayLength) {
			String errmsg = "LenStringCodec: not enough bytes for decode, need [" + arrayLength + "], actually ["
					+ bytes.length + "].";
			if (null != ctx.getField()) {
				errmsg += "/ cause field is [" + ctx.getField() + "]";
			}
			logger.error(errmsg);
			throw new RuntimeException(errmsg);
		}

		Object value = null;
		try {
			byte[] tmp = ArrayUtils.subarray(bytes, 0, arrayLength);
			value = new String(tmp, XIP_STR_CHARSET);
		} catch (UnsupportedEncodingException e) {
			logger.error("CStyleString", e);
		}

		return new DecResult(value, ArrayUtils.subarray(bytes, arrayLength, bytes.length));
	}

	@Override
	public byte[] encode(EncContext ctx) {
		String value = (String) ctx.getEncObject();

		if (null == value) {
			return new byte[] { 0, 0 };
		}

		byte[] bytes = null;
		try {
			bytes = value.getBytes(XIP_STR_CHARSET);
		} catch (UnsupportedEncodingException e) {
			logger.error("LenStringCodec", e);
		}

		return (byte[]) ArrayUtils
				.addAll(
						ctx.getCodecOf(short.class).encode(
								ctx.getEncContextFactory().createEncContext((short) (null == bytes ? 0 : bytes.length), short.class,
										null)), bytes);
	}

}
