package org.easycluster.easycluster.serialization.bytebean.context;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.ByteBeanUtil;
import org.easycluster.easycluster.serialization.bytebean.codec.ByteFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecCategory;
import org.easycluster.easycluster.serialization.bytebean.codec.FieldCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.bytebean.field.ByteFieldDesc;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: AbstractCodecContext.java 14 2012-01-10 11:54:14Z archie $
 */
public class AbstractCodecContext extends ByteBeanUtil implements FieldCodecContext {

	protected FieldCodecProvider	codecProvider	= null;

	protected ByteFieldDesc				fieldDesc;
	protected NumberCodec					numberCodec;
	protected Class<?>						targetType;

	@Override
	public ByteFieldDesc getFieldDesc() {
		return fieldDesc;
	}

	@Override
	public Field getField() {
		if (null != this.fieldDesc) {
			return this.fieldDesc.getField();
		} else {
			return null;
		}
	}

	@Override
	public NumberCodec getNumberCodec() {
		if (null != fieldDesc) {
			return DefaultNumberCodecs.getBigEndianNumberCodec();
		}
		return numberCodec;
	}

	@Override
	public int getByteSize() {
		int ret = -1;
		if (null != fieldDesc) {
			ret = fieldDesc.getByteSize();
		} else if (null != targetType) {
			ret = super.type2DefaultByteSize(targetType);
		}
		return ret;
	}

	public ByteFieldCodec getCodecOf(FieldCodecCategory type) {
		if (null != codecProvider) {
			return codecProvider.getCodecOf(type);
		} else {
			return null;
		}
	}

	public ByteFieldCodec getCodecOf(Class<?> clazz) {
		if (null != codecProvider) {
			return codecProvider.getCodecOf(clazz);
		} else {
			return null;
		}
	}

}
