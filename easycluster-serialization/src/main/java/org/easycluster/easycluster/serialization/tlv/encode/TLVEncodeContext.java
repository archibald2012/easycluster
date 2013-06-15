/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.lang.reflect.Field;

import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;

public interface TLVEncodeContext {
	Class<?> getValueType();

	Field getValueField();

	NumberCodec getNumberCodec();

	TLVEncoderRepository getEncoderRepository();
}
