package org.easycluster.easycluster.serialization.tlv.decode.decoders;

import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MapTLVDecoder implements TLVDecoder {

	private static Logger	logger	= LoggerFactory.getLogger(MapTLVDecoder.class);

	public Object decode(int tlvLength, byte[] tlvValue, TLVDecodeContext ctx) {
		if (tlvValue.length < tlvLength) {
			logger.error("MapTLVDecoder, too few bytes {}.", tlvLength);
			throw new RuntimeException("MapTLVDecoder, too few bytes.");
		}

		Field field = ctx.getValueField();
		Type type = field.getGenericType();
		if (!(type instanceof ParameterizedType)) {
			logger.error("can't decode unparameterized map.");
			throw new RuntimeException("can't decode unparameterized map.");
		}
		ParameterizedType pType = (ParameterizedType) type;
		Type[] actTypes = pType.getActualTypeArguments();
		if (null == actTypes || actTypes.length < 2) {
			logger.error("parameterized args less than 2.");
			throw new RuntimeException("parameterized args less than 2.");
		}

		Type keyType = actTypes[0];
		Type valueType = actTypes[1];

		TLVDecoder keyDecoder = ctx.getDecoderRepository().getDecoderOf((Class<?>) keyType);
		TLVDecoder valueDecoder = ctx.getDecoderRepository().getDecoderOf((Class<?>) valueType);

		if (null == keyDecoder || null == valueDecoder) {
			throw new RuntimeException("unknown decoder for [type]: " + keyType + " and " + valueType);
		}

		Map<Object, Object> target = new HashMap<Object, Object>();

		int offset = 0;
		boolean isDecodeKey = true;
		Object entryKey = null;
		Object entryValue = null;

		while (offset + 4 <= tlvLength) {
			byte[] lenBytes = ArrayUtils.subarray(tlvValue, offset, offset + 4);
			int len = ctx.getNumberCodec().bytes2Int(lenBytes, 4);
			offset += 4;

			byte[] objBytes = ArrayUtils.subarray(tlvValue, offset, offset + len);
			offset += len;
			try {
				if (isDecodeKey) {
					entryKey = keyDecoder.decode(len, objBytes, ctx);
				} else {
					entryValue = valueDecoder.decode(len, objBytes, ctx);
					target.put(entryKey, entryValue);
				}
				isDecodeKey = !isDecodeKey;
			} catch (RuntimeException e) {
				// logger.error("Decode tag {}({}) error!",new
				// String(Hex.encodeHex(tagBytes)).toUpperCase(),tag);
				throw e;
			}
		}
		return target;
	}
}
