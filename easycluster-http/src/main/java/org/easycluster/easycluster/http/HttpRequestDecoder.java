package org.easycluster.easycluster.http;

import java.io.UnsupportedEncodingException;

import org.apache.commons.lang.math.NumberUtils;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.serialization.Serialization;
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
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpRequestDecoder implements Transformer<HttpRequest, Object> {

	private static final Logger					LOGGER				= LoggerFactory.getLogger(HttpRequestDecoder.class);

	private BeanFieldCodec						beanFieldCodec		= null;
	private Int2TypeMetainfo					typeMetaInfo		= null;
	private boolean								isDebugEnabled		= false;
	private int									dumpBytes			= 256;
	private Transformer<HttpRequest, String>	requestCodeGetter	= new RequestCodeGetter();

	private Serialization						serialization		= null;

	@Override
	public Object transform(HttpRequest request) {

		ChannelBuffer content = request.getContent();
		byte[] bytes = new byte[content.readableBytes()];
		content.readBytes(bytes);

		if (LOGGER.isDebugEnabled() && isDebugEnabled) {
			LOGGER.debug("uri {}, bytes --> {}", request.getUri(), ByteUtil.bytesAsHexString(bytes, dumpBytes));
		}

		String requestCode = requestCodeGetter.transform(request);
		if (!NumberUtils.isDigits(requestCode)) {
			if (LOGGER.isDebugEnabled()) {
				LOGGER.debug("request code {} is not digit, ignore.", requestCode);
			}
			return null;
		}
		int messageCode = Integer.parseInt(requestCode);

		Class<?> type = typeMetaInfo.find(messageCode);
		if (null == type) {
			throw new InvalidMessageException("unknown message code:" + messageCode);
		}

		if (bytes.length == 0) {
			String uri = request.getUri().trim();
			// for eg:
			// http://appid.fivesky.net:4009/UpdateProvision?param1=111&param2=222
			int idx = uri.indexOf('?');
			if (-1 != idx) {
				String query = uri.substring(idx + 1);
				try {
					bytes = query.getBytes("UTF-8");
				} catch (UnsupportedEncodingException ignore) {
				}
			}
		}

		XipSignal signal = (XipSignal) serialization.deserialize(bytes, type);

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

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
	}

	public void setTypeMetaInfo(Int2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public void setRequestCodeGetter(Transformer<HttpRequest, String> requestCodeGetter) {
		this.requestCodeGetter = requestCodeGetter;
	}

}
