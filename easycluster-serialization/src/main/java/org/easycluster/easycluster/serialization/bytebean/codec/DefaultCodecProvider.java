
package org.easycluster.easycluster.serialization.bytebean.codec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;



/**
 * 默认字段解码器提供商
 * 
 * @author wangqi
 * @version $Id: DefaultCodecProvider.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultCodecProvider implements FieldCodecProvider {

	private final Map<Class<?>, ByteFieldCodec> class2Codec = new HashMap<Class<?>, ByteFieldCodec>();

	private final Map<FieldCodecCategory, ByteFieldCodec> category2Codec = new HashMap<FieldCodecCategory, ByteFieldCodec>();

	public void setCodecs(Collection<ByteFieldCodec> codecs) {
		for (ByteFieldCodec codec : codecs) {
			addCodec(codec);
		}
	}

	public DefaultCodecProvider addCodec(ByteFieldCodec codec) {
		Class<?>[] classes = codec.getFieldType();

		if (null != classes) {
			for (Class<?> clazz : classes) {
				class2Codec.put(clazz, codec);
			}
		} else if (null != codec.getCategory()) {
			category2Codec.put(codec.getCategory(), codec);
		}

		return this;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.FieldCodecProvider#
	 * getCodecOf
	 * (com.taotaosou.common.serialization.bytebean.field.FieldCodecCategory)
	 */
	@Override
	public ByteFieldCodec getCodecOf(FieldCodecCategory type) {
		return category2Codec.get(type);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * com.taotaosou.common.serialization.bytebean.field.FieldCodecProvider#
	 * getCodecOf(java.lang.Class)
	 */
	@Override
	public ByteFieldCodec getCodecOf(Class<?> clazz) {
		return class2Codec.get(clazz);
	}

}
