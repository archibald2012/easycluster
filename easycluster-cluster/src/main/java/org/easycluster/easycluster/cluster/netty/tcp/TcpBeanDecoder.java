package org.easycluster.easycluster.cluster.netty.tcp;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.easycluster.easycluster.cluster.exception.InvalidMessageException;
import org.easycluster.easycluster.cluster.netty.serialization.Serialization;
import org.easycluster.easycluster.core.ByteUtil;
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
import org.easycluster.easycluster.serialization.protocol.xip.AbstractXipSignal;
import org.easycluster.easycluster.serialization.protocol.xip.XipHeader;
import org.easymetrics.easymetrics.MetricsCollectorFactory;
import org.easymetrics.easymetrics.engine.MetricsCollector;
import org.easymetrics.easymetrics.engine.MetricsTimer;
import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.channel.Channel;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.handler.codec.frame.FrameDecoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TcpBeanDecoder extends FrameDecoder {

	private static final Logger				LOGGER				= LoggerFactory.getLogger(TcpBeanDecoder.class);
	private static final MetricsCollector	COLLECTOR			= MetricsCollectorFactory.getMetricsCollector(TcpBeanDecoder.class);

	private BeanFieldCodec					beanFieldCodec		= null;
	private Int2TypeMetainfo				typeMetaInfo		= null;
	private int								dumpBytes			= 256;
	private boolean							isDebugEnabled		= true;
	private int								maxMessageLength	= -1;
	private Serialization					serialization		= null;

	@Override
	protected Object decode(ChannelHandlerContext ctx, Channel channel, ChannelBuffer buffer) throws Exception {

		if (!buffer.readable()) {
			return null;
		}

		int headerSize = XipHeader.HEADER_LENGTH;

		XipHeader header = (XipHeader) channel.getAttachment();

		if (header == null) {
			if (buffer.readableBytes() < headerSize) {
				return null;
			} else {

				byte[] headerBytes = new byte[headerSize];
				buffer.readBytes(headerBytes);

				header = (XipHeader) getBeanFieldCodec().decode(
						getBeanFieldCodec().getDecContextFactory().createDecContext(headerBytes, XipHeader.class, null, null)).getValue();

				if (LOGGER.isDebugEnabled() && isDebugEnabled) {
					LOGGER.debug("header raw bytes --> {}, decoded XipHeader {}", ByteUtil.bytesAsHexString(headerBytes, dumpBytes),
							ToStringBuilder.reflectionToString(header, ToStringStyle.SHORT_PREFIX_STYLE));
				}

				if (maxMessageLength > 0) {
					if (header.getLength() > maxMessageLength) {
						LOGGER.error("header.length (" + header.getLength() + ") exceed maxMessageLength[" + maxMessageLength
								+ "], so drop this connection.\r\ndump bytes received:\r\n" + ByteUtil.bytesAsHexString(headerBytes, dumpBytes));
						channel.close();
						return null;
					}
				}

				channel.setAttachment(header);
			}

		}

		int bodySize = header.getLength() - headerSize;

		if (buffer.readableBytes() < bodySize) {
			return null;
		} else {

			channel.setAttachment(null);

			Exception exception = null;
			MetricsTimer metricsTimer = COLLECTOR.startMetricsTimer("decode");
			metricsTimer.addMetrics(header.getLength());
			try {
				byte[] bodyBytes = new byte[bodySize];
				buffer.readBytes(bodyBytes);

				Class<?> type = typeMetaInfo.find(header.getMessageCode());
				if (null == type) {
					throw new InvalidMessageException("unknown message code:" + header.getMessageCode());
				}

				AbstractXipSignal signal = (AbstractXipSignal) serialization.deserialize(bodyBytes, type);

				if (null != signal) {
					signal.setIdentification(header.getSequence());
					signal.setClient(header.getClientId());
				}

				return signal;
			} catch (RuntimeException ex) {
				exception = ex;
				throw ex;
			} finally {
				metricsTimer.stop(exception);
			}
		}

	}

	public void setSerialization(Serialization serialization) {
		this.serialization = serialization;
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

	public void setMaxMessageLength(int maxMessageLength) {
		this.maxMessageLength = maxMessageLength;
	}

	public void setTypeMetaInfo(Int2TypeMetainfo typeMetaInfo) {
		this.typeMetaInfo = typeMetaInfo;
	}

	public void setDumpBytes(int dumpBytes) {
		this.dumpBytes = dumpBytes;
	}

	public Int2TypeMetainfo getTypeMetaInfo() {
		return typeMetaInfo;
	}

	public int getDumpBytes() {
		return dumpBytes;
	}

	public boolean isDebugEnabled() {
		return isDebugEnabled;
	}

	public void setDebugEnabled(boolean isDebugEnabled) {
		this.isDebugEnabled = isDebugEnabled;
	}
}
