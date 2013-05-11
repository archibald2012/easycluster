/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.meta;

import java.lang.reflect.Field;

public interface TLVFieldMetainfo {
	Field get(int tag);
}
