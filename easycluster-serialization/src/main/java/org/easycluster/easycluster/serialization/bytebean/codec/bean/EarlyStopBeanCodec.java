
package org.easycluster.easycluster.serialization.bytebean.codec.bean;

import java.lang.reflect.Field;
import java.util.List;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.AbstractCategoryCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecCategory;
import org.easycluster.easycluster.serialization.bytebean.context.DecContext;
import org.easycluster.easycluster.serialization.bytebean.context.DecContextFactory;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.EncContext;
import org.easycluster.easycluster.serialization.bytebean.context.EncContextFactory;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;
import org.easycluster.easycluster.serialization.bytebean.field.Field2Desc;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 * TODO
 * 
 * @author Archibald.Wang
 * @version $Id: EarlyStopBeanCodec.java 14 2012-01-10 11:54:14Z archie $
 */
public class EarlyStopBeanCodec extends AbstractCategoryCodec implements
		BeanFieldCodec {

	private static final Logger logger = LoggerFactory
			.getLogger(EarlyStopBeanCodec.class);

	private DecContextFactory decContextFactory;
	private EncContextFactory encContextFactory;

	private BeanCodecUtil util;

	public EarlyStopBeanCodec(Field2Desc field2Desc) {
		util = new BeanCodecUtil(field2Desc);
	}

	@Override
	public DecResult decode(DecContext ctx) {
		byte[] bytes = ctx.getDecBytes();
		Class<?> clazz = ctx.getDecClass();

		Object target = null;

		try {
			target = clazz.newInstance();

			List<ByteFieldDesc> desces = util.getFieldDesces(clazz);

			ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

			for (ByteFieldDesc desc : desces) {
				if (0 == bytes.length) {
					break;
				}

				Field field = desc.getField();

				Class<?> fieldClass = field.getType();

				DecResult ret = anyCodec.decode(decContextFactory
						.createDecContext(bytes, fieldClass, target, desc));

				Object fieldValue = ret.getValue();
				bytes = ret.getRemainBytes();

				field.setAccessible(true);
				field.set(target, fieldValue);
			}

		} catch (InstantiationException e) {
			logger.error("BeanCodec:", e);
		} catch (IllegalAccessException e) {
			logger.error("BeanCodec:", e);
		}

		return new DecResult(target, bytes);
	}
	@Override
	public byte[] encode(EncContext ctx) {
		Object bean = ctx.getEncObject();
		if (null == bean) {
			String errmsg = "EarlyStopBeanCodec: bean is null";
			if (null != ctx.getField()) {
				errmsg += "/ cause field is [" + ctx.getField() + "]";
			} else {
				errmsg += "/ cause type is [" + ctx.getEncClass() + "]";
			}
			logger.error(errmsg);
			throw new RuntimeException(errmsg);
		}
		List<ByteFieldDesc> desces = util.getFieldDesces(bean.getClass());
		byte[] ret = new byte[0];
		ByteFieldCodec anyCodec = ctx.getCodecOf(FieldCodecCategory.ANY);

		for (ByteFieldDesc desc : desces) {
			Field field = desc.getField();
			Class<?> fieldClass = field.getType();
			field.setAccessible(true);
			Object fieldValue = null;

			try {
				fieldValue = field.get(bean);
			} catch (IllegalArgumentException e) {
				logger.error("BeanCodec:", e);
			} catch (IllegalAccessException e) {
				logger.error("BeanCodec:", e);
			}

			ret = (byte[]) ArrayUtils.addAll(ret, anyCodec
					.encode(encContextFactory.createEncContext(fieldValue,
							fieldClass, desc)));
		}

		return ret;
	}

	@Override
	public FieldCodecCategory getCategory() {
		return FieldCodecCategory.BEAN;
	}

	@Override
	public int getStaticByteSize(Class<?> clazz) {

		List<ByteFieldDesc> desces = util.getFieldDesces(clazz);

		if (null == desces || desces.isEmpty()) {
			return -1;
		}

		int staticByteSize = 0;

		for (ByteFieldDesc desc : desces) {
			int fieldByteSize = desc.getByteSize();

			if (fieldByteSize <= 0) {
				fieldByteSize = getStaticByteSize(desc.getFieldType());
			}

			if (fieldByteSize <= 0) {
				return -1;
			}
			staticByteSize += fieldByteSize;
		}

		return staticByteSize;
	}

	@Override
	public DecContextFactory getDecContextFactory() {
		return decContextFactory;
	}

	public void setDecContextFactory(DecContextFactory decContextFactory) {
		this.decContextFactory = decContextFactory;
	}

	@Override
	public EncContextFactory getEncContextFactory() {
		return encContextFactory;
	}

	public void setEncContextFactory(EncContextFactory encContextFactory) {
		this.encContextFactory = encContextFactory;
	}

}
