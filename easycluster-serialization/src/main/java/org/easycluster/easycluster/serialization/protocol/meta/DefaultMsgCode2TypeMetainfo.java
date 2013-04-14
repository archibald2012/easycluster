package org.easycluster.easycluster.serialization.protocol.meta;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: DefaultMsgCode2TypeMetainfo.java 14 2012-01-10 11:54:14Z archie $
 */
public class DefaultMsgCode2TypeMetainfo implements MsgCode2TypeMetainfo {

	private Map<Integer, Class<?>>	codes	= new HashMap<Integer, Class<?>>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see com.taotaosou.common.transport.protocol.meta.MsgCode2TypeMetainfo#find (int)
	 */
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
