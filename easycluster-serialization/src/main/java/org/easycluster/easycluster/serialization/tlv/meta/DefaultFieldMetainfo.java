/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.meta;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

public class DefaultFieldMetainfo implements TLVFieldMetainfo {

	private Map<Integer, Field>	tags	= new HashMap<Integer, Field>();

	public void add(int tag, Field field) {
		tags.put(tag, field);
	}

	public Field get(int tag) {
		return tags.get(tag);
	}

}
