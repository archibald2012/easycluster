
package org.easycluster.easycluster.serialization.bytebean.codec.array;

import java.lang.reflect.Array;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.AbstractCategoryCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecCategory;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: ArrayCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class ArrayCodec extends AbstractCategoryCodec implements ByteFieldCodec {

	@Override
	public FieldCodecCategory getCategory() {
		return FieldCodecCategory.ARRAY;
	}

	@Override
	public DecResult decode(DecContext ctx) {
		byte[] bytes = ctx.getDecBytes();
		Class<?> fieldClass = ctx.getDecClass();
		Class<?> compomentClass = fieldClass.getComponentType();
		final ByteFieldDesc desc = ctx.getFieldDesc();
		int arrayLength = 0;

		if (null == desc || !desc.hasLength()) {
			throw new RuntimeException("invalid array env.");
		} else {
			arrayLength = desc.getLength(ctx.getDecOwner());
		}

		Object array = null;
		if (arrayLength > 0) {
			array = Array.newInstance(compomentClass, arrayLength);
			ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

			for (int idx = 0; idx < arrayLength; idx++) {
				DecResult ret = anyCodec.decode(ctx.getDecContextFactory()
						.createDecContext(bytes, compomentClass,
								ctx.getDecOwner(), null));
				Array.set(array, idx, ret.getValue());
				bytes = ret.getRemainBytes();
			}
		}

		return new DecResult(array, bytes);
	}

	@Override
	public byte[] encode(EncContext ctx) {
		Object array = ctx.getEncObject();
		Class<?> fieldClass = ctx.getEncClass();
		Class<?> compomentClass = fieldClass.getComponentType();
		int arrayLength = (null != array ? Array.getLength(array) : 0);

		ByteFieldDesc desc = ctx.getFieldDesc();
		byte[] bytes = null;

		if (null == desc || !desc.hasLength()) {
			throw new RuntimeException("invalid array env.");
		} else {
			// 
		}
		ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

		for (int idx = 0; idx < arrayLength; idx++) {
			bytes = ArrayUtils.addAll(bytes, anyCodec.encode(ctx
					.getEncContextFactory().createEncContext(
							Array.get(array, idx), compomentClass, null)));
		}
		return bytes;
	}

}
