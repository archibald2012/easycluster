/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.serialization.kv.FieldUtil;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoderOfBean;
import org.easycluster.easycluster.serialization.tlv.meta.TLVCodecUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BeanTLVEncoder implements TLVEncoderOfBean {

	private static final Logger		logger	= LoggerFactory.getLogger(BeanTLVEncoder.class);

	private TLVEncodeContextFactory	encodeContextFactory;

	@SuppressWarnings("unchecked")
	public List<byte[]> encode(Object tlvBean, TLVEncodeContext ctx) {
		if (null == tlvBean) {
			throw new RuntimeException("null == tlvBean.");
		}

		List<byte[]> ret = new ArrayList<byte[]>();
		Field[] fields = TLVCodecUtils.getTLVFieldsOf(tlvBean.getClass());

		for (Field field : fields) {
			TLVAttribute param = field.getAnnotation(TLVAttribute.class);

			if (null == param) {
				// not TLVAttribute
				continue;
			}

			field.setAccessible(true);
			Object src = null;
			try {
				src = field.get(tlvBean);
			} catch (IllegalArgumentException e) {
				logger.error("transform:", e);
			} catch (IllegalAccessException e) {
				logger.error("transform:", e);
			}
			if (null == src) {
				continue;
			}

			Class<?> type = param.type();
			if (TLVAttribute.class.equals(type)) {
				type = src.getClass();
			}

			if (type.equals(ArrayList.class)) {
				Class<?> componentType = FieldUtil.getComponentClass(field);
				TLVEncoder encoder = ctx.getEncoderRepository().getEncoderOf(componentType);
				if (null == encoder) {
					logger.error("field[" + field + "]/" + componentType.getSimpleName() + " can not found encoder, ignore");
					continue;
				}
				ArrayList<Object> list = (ArrayList<Object>) src;
				for (Object component : list) {
					List<byte[]> dest = encoder.encode(component, encodeContextFactory.createEncodeContext(componentType, field));
					// tag
					ret.add(ctx.getNumberCodec().int2Bytes(param.tag(), 4));

					// len
					ret.add(ctx.getNumberCodec().int2Bytes(ByteUtil.totalByteSizeOf(dest), 4));

					ret.addAll(dest);
				}
			} else {
				TLVEncoder encoder = ctx.getEncoderRepository().getEncoderOf(type);
				if (null == encoder) {
					logger.error("field[" + field + "] can not found encoder, ignore");
					continue;
				}
				List<byte[]> dest = encoder.encode(src, encodeContextFactory.createEncodeContext(type, field));

				// tag
				ret.add(ctx.getNumberCodec().int2Bytes(param.tag(), 4));

				// len
				ret.add(ctx.getNumberCodec().int2Bytes(ByteUtil.totalByteSizeOf(dest), 4));

				ret.addAll(dest);
			}
		}

		return ret;
	}

	public TLVEncodeContextFactory getEncodeContextFactory() {
		return encodeContextFactory;
	}

	public void setEncodeContextFactory(TLVEncodeContextFactory encodeContextFactory) {
		this.encodeContextFactory = encodeContextFactory;
	}

}
