package org.easycluster.easycluster.cluster.netty.codec;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.kv.codec.DefaultKVCodec;
import org.easycluster.easycluster.serialization.kv.codec.KVCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class KVBeanEncoder implements Transformer<Object, byte[]> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(KVBeanEncoder.class);

	private static final String	ENCODING		= "UTF-8";

	private KVCodec				kvCodec			= new DefaultKVCodec();
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= true;

	@Override
	public byte[] transform(Object object) {

		String string = kvCodec.encode(kvCodec.getEncContextFactory().createEncContext(object, object.getClass()));

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and KV string --> {}", ToStringBuilder.reflectionToString(object), string);
		}

		try {
			byte[] bytes = string.getBytes(ENCODING);
			if (LOGGER.isDebugEnabled() && isDebugEnabled) {
				LOGGER.debug("encode signal {}, and signal raw bytes --> {}", ToStringBuilder.reflectionToString(object),
						ByteUtil.bytesAsHexString(bytes, dumpBytes));
			}
			return bytes;
		} catch (UnsupportedEncodingException ignore) {
			return null;
		}

	}

	public void setKvCodec(KVCodec kvCodec) {
		this.kvCodec = kvCodec;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

}
