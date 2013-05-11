/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.store;

import java.util.List;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.serialization.tlv.annotation.TLVAttribute;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecodeContext;
import org.easycluster.easycluster.serialization.tlv.decode.TLVDecoderOfBean;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BeanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoderOfBean;
import org.easycluster.easycluster.serialization.tlv.meta.Int2TypeMetainfo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TLVStore {

	private static final Logger					logger	= LoggerFactory.getLogger(TLVStore.class);

	private ConcurrentHashMap<String, byte[]>	map		= new ConcurrentHashMap<String, byte[]>();
	private TLVEncoderOfBean					beanEncoder;
	private TLVDecoderOfBean					beanDecoder;
	private Int2TypeMetainfo					typeMetainfo;

	public TLVEncoderOfBean getBeanEncoder() {
		return beanEncoder;
	}

	public void setBeanEncoder(TLVEncoderOfBean beanEncoder) {
		this.beanEncoder = beanEncoder;
	}

	public TLVDecoderOfBean getBeanDecoder() {
		return beanDecoder;
	}

	public void setBeanDecoder(BeanTLVDecoder beanDecoder) {
		this.beanDecoder = beanDecoder;
	}

	public Int2TypeMetainfo getTypeMetainfo() {
		return typeMetainfo;
	}

	public void setTypeMetainfo(Int2TypeMetainfo typeMetainfo) {
		this.typeMetainfo = typeMetainfo;
	}

	public void setTLV(String key, Object tlv) {
		if (null == tlv) {
			return;
		}

		TLVEncodeContext ctx = beanEncoder.getEncodeContextFactory().createEncodeContext(tlv.getClass(), null);
		List<byte[]> byteList = beanEncoder.encode(tlv, ctx);

		TLVAttribute attr = tlv.getClass().getAnnotation(TLVAttribute.class);
		if (null == attr) {
			logger.error("setTLV: invalid top tlv object, missing @TLVAttribute.");
			throw new RuntimeException("invalid top tlv object, missing @TLVAttribute.");
		}

		byteList.add(0, ctx.getNumberCodec().int2Bytes(attr.tag(), 4));
		byte[] bytesBody = ByteUtil.union(byteList);
		map.put(key, bytesBody);
	}

	public Object getTLV(String key) {
		byte[] bytes = (byte[]) map.get(key);

		if (null == bytes) {
			return null;
		}

		TLVDecodeContext ctx = beanDecoder.getDecodeContextFactory().createDecodeContext(null, null);

		int tag = ctx.getNumberCodec().bytes2Int(bytes, 4);
		Class<?> type = typeMetainfo.find(tag);

		if (null == type) {
			logger.error("getTLV: unknow tag:" + tag);
			throw new RuntimeException("unknow tag:" + tag);
		}

		byte[] bytesBody = ArrayUtils.subarray(bytes, 4, bytes.length);
		Object tlv = beanDecoder.decode(bytesBody.length, bytesBody, beanDecoder.getDecodeContextFactory().createDecodeContext(type, null));
		return tlv;
	}
}
