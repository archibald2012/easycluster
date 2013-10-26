package org.easycluster.easycluster.http;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.serialization.Serialization;
import org.easycluster.easycluster.cluster.serialization.SerializationConfig;
import org.easycluster.easycluster.core.ByteUtil;
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
import org.easycluster.easycluster.serialization.protocol.meta.Int2TypeMetainfo;
import org.easycluster.easycluster.serialization.protocol.xip.XipSignal;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.handler.codec.http.HttpResponse;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpResponseDecoder implements Transformer<HttpResponse, Object> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpResponseDecoder.class);

	private BeanFieldCodec		beanFieldCodec	= null;
	private Int2TypeMetainfo	typeMetaInfo	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;

	private Serialization		serialization;

	@Override
	public Object transform(HttpResponse from) {
		if (from.getStatus().getCode() != HttpResponseStatus.OK.getCode()) {
			return null;
		}

		ChannelBuffer content = from.getContent();
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes(bytes);

		byte[] headerBytes = ArrayUtils.subarray(bytes, 0, 4);

		int messageCode = getBeanFieldCodec().getDecContextFactory().createDecContext(headerBytes, Integer.class, null, null).getNumberCodec()
				.bytes2Int(headerBytes, headerBytes.length);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("header bytes --> {}, decoded messageCode {}", ByteUtil.bytesAsHexString(headerBytes, dumpBytes), messageCode);
		}

		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknown message code:" + messageCode);
		}

		byte[] bodyBytes = ArrayUtils.subarray(bytes, 4, bytes.length);

		XipSignal signal = (XipSignal) serialization.deserialize(bodyBytes, type);

		return signal;

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

	public void setSerializationConfig(SerializationConfig serializationConfig) {
		this.typeMetaInfo = serializationConfig.getTypeMetaInfo();
		this.dumpBytes = serializationConfig.getDumpBytes();
		this.isDebugEnabled = serializationConfig.isSerializeBytesDebugEnabled();
	}

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
	}

}
