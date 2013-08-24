package org.easycluster.easycluster.serialization.bytebean.codec;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultCodecProvider implements FieldCodecProvider {

	private final Map<Class<?>, ByteFieldCodec>				class2Codec		= new HashMap<Class<?>, ByteFieldCodec>();

	private final Map<FieldCodecCategory, ByteFieldCodec>	category2Codec	= new HashMap<FieldCodecCategory, ByteFieldCodec>();

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

	@Override
	public ByteFieldCodec getCodecOf(FieldCodecCategory type) {
		return category2Codec.get(type);
	}

	@Override
	public ByteFieldCodec getCodecOf(Class<?> clazz) {
		return class2Codec.get(clazz);
	}

}
