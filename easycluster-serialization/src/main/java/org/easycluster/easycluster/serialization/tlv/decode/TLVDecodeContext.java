/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.tlv.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.tlv.meta.TLVFieldMetainfo;

public interface TLVDecodeContext {
	Class<?> getValueType();

	Field getValueField();

	Int2TypeMetainfo getTypeMetainfo();

	TLVFieldMetainfo getFieldMetainfo();

	NumberCodec getNumberCodec();

	TLVDecoderRepository getDecoderRepository();
}
