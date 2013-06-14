package org.easycluster.easycluster.serialization.protocol.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class DefaultMsgCode2TypeMetainfo implements MsgCode2TypeMetainfo {

	private Map<Integer, Class<?>>	codes	= new HashMap<Integer, Class<?>>();

	@Override
	public Class<?> find(int value) {
		return codes.get(value);
	}

	public void add(int tag, Class<?> type) {
		codes.put(tag, type);
	}

	public Collection<Class<?>> getClasses() {
		return codes.values();
	}

	public Map<Integer, String> getAllMetainfo() {
		Map<Integer, String> ret = new HashMap<Integer, String>();
		for (Map.Entry<Integer, Class<?>> entry : this.codes.entrySet()) {
			ret.put(entry.getKey(), entry.getValue().toString());
		}

		return ret;
	}
}
