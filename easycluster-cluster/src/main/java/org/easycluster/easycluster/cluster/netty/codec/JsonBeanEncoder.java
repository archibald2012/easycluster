package org.easycluster.easycluster.cluster.netty.codec;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.core.DES;
import org.easycluster.easycluster.core.Transformer;
import org.easycluster.easycluster.serialization.bytebean.codec.AnyCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.array.LenArrayCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.array.LenListCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.bean.BeanFieldCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.bean.EarlyStopBeanCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.BooleanCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.ByteCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.DoubleCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.FloatCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.IntCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LenByteArrayCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LenStringCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LongCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.ShortCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultDecContextFactory;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultEncContextFactory;
import org.easycluster.easycluster.serialization.bytebean.field.DefaultField2Desc;
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.alibaba.fastjson.JSON;

public class JsonBeanEncoder implements Transformer<XipSignal, byte[]> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(JsonBeanEncoder.class);

	private static final String	ENCODING		= "UTF-8";

	private BeanFieldCodec		beanFieldCodec	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;
	private byte[]				encryptKey		= null;

	@Override
	public byte[] transform(XipSignal signal) {
		SignalCode attr = signal.getClass().getAnnotation(SignalCode.class);
		if (null == attr) {
			throw new InvalidMessageException("invalid signal, no messageCode defined.");
		}

		String jsonString = JSON.toJSONString(signal);
		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and body string --> {}", ToStringBuilder.reflectionToString(signal), jsonString);
		}

		byte[] bodyBytes = null;
		try {
			bodyBytes = jsonString.getBytes(ENCODING);
		} catch (UnsupportedEncodingException ignore) {
		}

		byte[] messageCodeBytes = getBeanFieldCodec().getEncContextFactory().createEncContext(new Integer(attr.messageCode()), Integer.class, null)
				.getNumberCodec().int2Bytes(attr.messageCode(), 4);

		byte[] bytes = ArrayUtils.addAll(messageCodeBytes, bodyBytes);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encoded bytes --> {}", ByteUtil.bytesAsHexString(bodyBytes, dumpBytes));
		}

		if (bodyBytes.length > 0 && encryptKey != null) {
			try {
				bodyBytes = DES.encryptThreeDESECB(bodyBytes, encryptKey);
			} catch (Exception e) {
				String error = "Failed to encrypt the body due to error " + e.getMessage();
				LOGGER.error(error, e);
				throw new InvalidMessageException(error, e);
			}
		}

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and signal raw bytes --> {}", ToStringBuilder.reflectionToString(signal),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		return bytes;
	}

	public void setBeanFieldCodec(BeanFieldCodec beanFieldCodec) {
		this.beanFieldCodec = beanFieldCodec;
	}

	public BeanFieldCodec getBeanFieldCodec() {
		if (beanFieldCodec == null) {
			DefaultCodecProvider codecProvider = new DefaultCodecProvider();

			codecProvider.addCodec(new AnyCodec()).addCodec(new ByteCodec()).addCodec(new ShortCodec()).addCodec(new IntCodec()).addCodec(new LongCodec())
					.addCodec(new BooleanCodec()).addCodec(new FloatCodec()).addCodec(new DoubleCodec()).addCodec(new LenStringCodec())
					.addCodec(new LenByteArrayCodec()).addCodec(new LenListCodec()).addCodec(new LenArrayCodec());

			EarlyStopBeanCodec beanCodec = new EarlyStopBeanCodec(new DefaultField2Desc());
			codecProvider.addCodec(beanCodec);

			DefaultEncContextFactory encContextFactory = new DefaultEncContextFactory();
			DefaultDecContextFactory decContextFactory = new DefaultDecContextFactory();

			encContextFactory.setCodecProvider(codecProvider);
			encContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			decContextFactory.setCodecProvider(codecProvider);
			decContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

			beanCodec.setDecContextFactory(decContextFactory);
			beanCodec.setEncContextFactory(encContextFactory);

			this.beanFieldCodec = beanCodec;
		}
		return beanFieldCodec;
	}

	public int getDumpBytes() {
		return dumpBytes;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	public void setEncryptKey(String encryptKey) {
		if (encryptKey != null) {
			this.encryptKey = encryptKey.getBytes();
		}
	}

}
