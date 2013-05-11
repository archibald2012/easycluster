/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

public interface TLVEncoderRepository {
	TLVEncoder getEncoderOf(Class<?> cls);
}
