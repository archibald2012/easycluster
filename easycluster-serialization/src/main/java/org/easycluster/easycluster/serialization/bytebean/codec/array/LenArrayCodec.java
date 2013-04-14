
package org.easycluster.easycluster.serialization.bytebean.codec.array;

import java.lang.reflect.Array;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.AbstractCategoryCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecCategory;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: LenArrayCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class LenArrayCodec extends AbstractCategoryCodec implements
		ByteFieldCodec {

	@Override
	public FieldCodecCategory getCategory() {
		return FieldCodecCategory.ARRAY;
	}

	@Override
	public DecResult decode(DecContext ctx) {
		DecResult ret = ctx.getCodecOf(int.class).decode(
				ctx.getDecContextFactory().createDecContext(ctx.getDecBytes(),
						int.class, ctx.getDecOwner(), null));
		int arrayLength = (Integer) ret.getValue();
		byte[] bytes = ret.getRemainBytes();

		Object array = null;
		if (arrayLength > 0) {
			Class<?> fieldClass = ctx.getDecClass();
			Class<?> compomentClass = fieldClass.getComponentType();

			array = Array.newInstance(compomentClass, arrayLength);
			ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

			for (int idx = 0; idx < arrayLength; idx++) {
				ret = anyCodec.decode(ctx.getDecContextFactory()
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
		int arrayLength = (null != array ? Array.getLength(array) : 0);

		byte[] bytes = ctx.getCodecOf(int.class).encode(
				ctx.getEncContextFactory().createEncContext(arrayLength,
						int.class, null));

		if (arrayLength > 0) {
			Class<?> fieldClass = ctx.getEncClass();
			Class<?> compomentClass = fieldClass.getComponentType();

			ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

			for (int idx = 0; idx < arrayLength; idx++) {
				bytes = ArrayUtils.addAll(bytes, anyCodec.encode(ctx
						.getEncContextFactory().createEncContext(
								Array.get(array, idx), compomentClass, null)));
			}
		}
		return bytes;
	}

}
