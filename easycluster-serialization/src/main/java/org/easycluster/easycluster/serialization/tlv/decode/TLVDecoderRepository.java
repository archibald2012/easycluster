/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode;

public interface TLVDecoderRepository {
	TLVDecoder getDecoderOf(Class<?> cls);
}
