package org.easycluster.easycluster.http;

import java.util.UUID;

import org.apache.commons.lang.ArrayUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
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
import org.easycluster.easycluster.serialization.protocol.annotation.SignalCode;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.handler.codec.http.DefaultHttpRequest;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpVersion;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestEncoder implements Transformer<Object, HttpRequest> {

	private static final Logger	LOGGER			= LoggerFactory.getLogger(HttpRequestEncoder.class);

	private BeanFieldCodec		beanFieldCodec	= null;
	private int					dumpBytes		= 256;
	private boolean				isDebugEnabled	= false;

	private Serialization		serialization;

	@Override
	public HttpRequest transform(Object message) {
		SignalCode attr = message.getClass().getAnnotation(SignalCode.class);
		if (null == attr) {
			throw new InvalidMessageException("invalid signal, no messageCode defined.");
		}

		byte[] headerBytes = getBeanFieldCodec().getEncContextFactory().createEncContext(Integer.valueOf(attr.messageCode()), Integer.class, null)
				.getNumberCodec().int2Bytes(attr.messageCode(), 4);

		byte[] bodyBytes = serialization.serialize(message);

		byte[] bytes = ArrayUtils.addAll(headerBytes, bodyBytes);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("encode signal {}, and signal raw bytes --> {}", ToStringBuilder.reflectionToString(message),
					ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		HttpRequest request = new DefaultHttpRequest(HttpVersion.HTTP_1_1, HttpMethod.POST, "/");

		request.setHeader("uuid", UUID.randomUUID());

		if (bytes != null) {
			request.setHeader(HttpHeaders.Names.CONTENT_LENGTH, bytes.length);
			// request.setHeader(HttpHeaders.Names.CONTENT_TYPE,
			// "application/json");
			request.setContent(ChannelBuffers.wrappedBuffer(bytes));
		}

		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug("encoded http request: [{}]", request);
		}

		return request;
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

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
	}

	public void setSerializationConfig(SerializationConfig serializationConfig) {
		this.dumpBytes = serializationConfig.getDumpBytes();
		this.isDebugEnabled = serializationConfig.isDecodeBytesDebugEnabled();
	}

}
