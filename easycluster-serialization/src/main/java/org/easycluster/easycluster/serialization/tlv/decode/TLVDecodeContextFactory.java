/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.decode;

import java.lang.reflect.Field;

public interface TLVDecodeContextFactory {
	TLVDecodeContext createDecodeContext(Class<?> type, Field field);
}
