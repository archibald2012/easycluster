/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.meta;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Collection;
import java.util.concurrent.Callable;

import org.easycluster.easycluster.core.SimpleCache;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.kv.FieldUtil;
import org.easycluster.easycluster.serialization.protocol.meta.DefaultInt2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLVCodecUtils {

	@SuppressWarnings("unused")
	private static final Logger							logger			= LoggerFactory.getLogger(TLVCodecUtils.class);

	private static final Transformer<Class<?>, Integer>	CLS2INT			= new Transformer<Class<?>, Integer>() {

																			public Integer transform(Class<?> clas) {
																				TLVAttribute attr = clas.getAnnotation(TLVAttribute.class);
																				return null != attr ? attr.tag() : null;
																			}
																		};

	private static SimpleCache<Class<?>, Field[]>		tlvFieldsCache	= new SimpleCache<Class<?>, Field[]>();

	static public Field[] getTLVFieldsOf(final Class<?> tlvType) {
		return tlvFieldsCache.get(tlvType, new Callable<Field[]>() {

			public Field[] call() throws Exception {
				return FieldUtil.getAnnotationFieldsOf(tlvType, TLVAttribute.class);
			}
		});
	}

	static public TLVFieldMetainfo createFieldMetainfo(Class<?> tlvType) {
		DefaultFieldMetainfo fieldMetainfo = new DefaultFieldMetainfo();

		Field[] fields = getTLVFieldsOf(tlvType);

		for (Field field : fields) {
			TLVAttribute param = field.getAnnotation(TLVAttribute.class);
			fieldMetainfo.add(param.tag(), field);
		}

		return fieldMetainfo;
	}

	static public TLVFieldMetainfo chainFieldMetainfo(final TLVFieldMetainfo first, final TLVFieldMetainfo second) {
		return new TLVFieldMetainfo() {

			public Field get(int tag) {
				Field field = first.get(tag);
				if (null != field) {
					return field;
				}

				return second.get(tag);
			}
		};
	}

	static public Int2TypeMetainfo createTypeMetainfo(Class<?> tlvType) {
		DefaultInt2TypeMetainfo typeMetainfo = new DefaultInt2TypeMetainfo();

		Field[] fields = getTLVFieldsOf(tlvType);

		for (Field field : fields) {
			TLVAttribute param = field.getAnnotation(TLVAttribute.class);

			Class<?> type = param.type();
			if (type.equals(TLVAttribute.class)) {
				type = field.getType();
				if (type.equals(ArrayList.class)) {
					type = FieldUtil.getComponentClass(field);
				}
			}
			typeMetainfo.add(param.tag(), type);
		}

		return typeMetainfo;
	}

	static public Int2TypeMetainfo createTopmostTypeMetainfo(Collection<String> packages) {
		return BeanMetainfoUtils.createTypeMetainfo(packages, CLS2INT);
	}

	static public Int2TypeMetainfo createTopmostTypeMetainfoByClasses(Collection<Class<?>> clazzes) {
		return BeanMetainfoUtils.createTypeMetainfoByClasses(clazzes, CLS2INT);
	}
}
