/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class DefaultEncoderRepository implements TLVEncoderRepository {

	private Map<Class<?>, TLVEncoder>	encoders	= new HashMap<Class<?>, TLVEncoder>();

	public void add(Class<?> cls, TLVEncoder encoder) {
		encoders.put(cls, encoder);
	}

	public void setEncoders(Map<Class<?>, TLVEncoder> encoders) {
		this.encoders.clear();

		for (Map.Entry<Class<?>, TLVEncoder> entry : encoders.entrySet()) {
			if (null != entry.getValue()) {
				this.encoders.put(entry.getKey(), entry.getValue());
			}
		}
	}

	public Map<Class<?>, TLVEncoder> getEncoders() {
		return Collections.unmodifiableMap(encoders);
	}

	public TLVEncoder getEncoderOf(Class<?> cls) {
		do {
			TLVEncoder encoder = encoders.get(cls);
			if (null != encoder) {
				return encoder;
			}
			if (Object.class.equals(cls)) {
				break;
			}
			cls = cls.getSuperclass();
		} while (cls != null);

		return null;
	}

	public Map<String, String> getEncodersAsText() {
		Map<String, String> ret = new HashMap<String, String>();
		for (Map.Entry<Class<?>, TLVEncoder> entry : this.encoders.entrySet()) {
			ret.put(entry.getKey().toString(), entry.getValue().toString());
		}

		return ret;
	}
}
