package org.easycluster.easycluster.cluster.netty.codec;

import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.kv.codec.DefaultKVCodec;
import org.easycluster.easycluster.serialization.kv.codec.KVCodec;
import org.easycluster.easycluster.serialization.protocol.meta.MsgCode2TypeMetainfo;

public class KVBeanDecoder implements Transformer<byte[], Object> {

	private KVCodec					kvCodec	= new DefaultKVCodec();

	private MsgCode2TypeMetainfo	typeMetaInfo;

	@Override
	public Object transform(byte[] from) {
		String uri = new String(from).trim();
		
		int messageCode = Integer.parseInt(getRequestCode(uri));
		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknow message code:" + messageCode);
		}

		String queryString = "";
		int idx = uri.indexOf('?');
		if (-1 != idx) {
			queryString = uri.substring(idx + 1); // escape '?' character and
													// more
		}

		// queryString = "".equals(queryString) ?
		// request.getContent().toString(CharsetUtil.UTF_8) : queryString + "&"
		// + request.getContent().toString(CharsetUtil.UTF_8);

		return kvCodec.decode(kvCodec.getDecContextFactory().createDecContext(queryString, type, null));
	}

	private String getRequestCode(String uri) {
		String requestCode = uri.trim();

		if (requestCode.startsWith("/")) {
			requestCode = requestCode.substring(1);
		}
		if (requestCode.endsWith("/")) {
			requestCode = requestCode.substring(0, requestCode.length() - 1);
		}

		// for eg: http://appid.fivesky.net:4009/UpdateProvision
		int idx = requestCode.lastIndexOf('/');
		if (-1 != idx) {
			requestCode = requestCode.substring(idx + 1); // escape '/'
															// character
		}

		// for eg: UpdateProvision?param1=111&param2=222
		idx = requestCode.indexOf('?');
		if (-1 != idx) {
			requestCode = requestCode.substring(0, idx); // escape '?' character
															// and
															// more
		}
		return requestCode.trim();
	}

	public void setKvCodec(KVCodec kvCodec) {
		this.kvCodec = kvCodec;
	}

	public void setTypeMetaInfo(MsgCode2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

}
