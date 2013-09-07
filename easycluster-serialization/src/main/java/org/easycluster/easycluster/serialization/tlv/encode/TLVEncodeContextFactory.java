/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.lang.reflect.Field;

public interface TLVEncodeContextFactory {
	TLVEncodeContext createEncodeContext(Class<?> type, Field field);
}
