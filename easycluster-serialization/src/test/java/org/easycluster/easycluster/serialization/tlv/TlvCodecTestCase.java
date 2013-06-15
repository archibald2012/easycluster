/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv;

import java.util.ArrayList;

import org.easycluster.easycluster.core.ByteUtil;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.decode.DefaultDecoderRepository;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BeanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.BooleanTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteArrayTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ByteTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.IntTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.LongTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.ShortTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.decode.decoders.StringTLVDecoder;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncodeContextFactory;
import org.easycluster.easycluster.serialization.tlv.encode.DefaultEncoderRepository;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BeanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.BooleanTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteArrayTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ByteTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.IntTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.LongTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.ShortTLVEncoder;
import org.easycluster.easycluster.serialization.tlv.encode.encoders.StringTLVEncoder;
import org.junit.Test;

public class TlvCodecTestCase {

	@Test
	public void testCodec() {

		DefaultEncoderRepository encoderRepository = new DefaultEncoderRepository();
		encoderRepository.add(byte[].class, new ByteArrayTLVEncoder());
		encoderRepository.add(int.class, new IntTLVEncoder());
		encoderRepository.add(Integer.class, new IntTLVEncoder());
		encoderRepository.add(byte.class, new ByteTLVEncoder());
		encoderRepository.add(Byte.class, new ByteTLVEncoder());
		encoderRepository.add(short.class, new ShortTLVEncoder());
		encoderRepository.add(Short.class, new ShortTLVEncoder());
		encoderRepository.add(long.class, new LongTLVEncoder());
		encoderRepository.add(Long.class, new LongTLVEncoder());
		encoderRepository.add(boolean.class, new BooleanTLVEncoder());
		encoderRepository.add(Boolean.class, new BooleanTLVEncoder());
		encoderRepository.add(String.class, new StringTLVEncoder());

		DefaultEncodeContextFactory encodeContextFactory = new DefaultEncodeContextFactory();
		encodeContextFactory.setEncoderRepository(encoderRepository);
		encodeContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

		BeanTLVEncoder beanEncoder = new BeanTLVEncoder();
		beanEncoder.setEncodeContextFactory(encodeContextFactory);

		encoderRepository.add(Object.class, beanEncoder);

		DefaultDecoderRepository decoderRepository = new DefaultDecoderRepository();
		decoderRepository.add(byte[].class, new ByteArrayTLVDecoder());
		decoderRepository.add(int.class, new IntTLVDecoder());
		decoderRepository.add(Integer.class, new IntTLVDecoder());
		decoderRepository.add(byte.class, new ByteTLVDecoder());
		decoderRepository.add(Byte.class, new ByteTLVDecoder());
		decoderRepository.add(short.class, new ShortTLVDecoder());
		decoderRepository.add(Short.class, new ShortTLVDecoder());
		decoderRepository.add(long.class, new LongTLVDecoder());
		decoderRepository.add(Long.class, new LongTLVDecoder());
		decoderRepository.add(boolean.class, new BooleanTLVDecoder());
		decoderRepository.add(Boolean.class, new BooleanTLVDecoder());
		decoderRepository.add(String.class, new StringTLVDecoder());

		DefaultDecodeContextFactory decodeContextFactory = new DefaultDecodeContextFactory();
		decodeContextFactory.setDecoderRepository(decoderRepository);
		decodeContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

		BeanTLVDecoder beanDecoder = new BeanTLVDecoder();
		beanDecoder.setDecodeContextFactory(decodeContextFactory);

		decoderRepository.add(Object.class, beanDecoder);

		SampleTLV sample = new SampleTLV();
		sample.setAge(101);
		sample.setName("hello, world!");
		ArrayList<ChildTLV> children = new ArrayList<ChildTLV>();
		ChildTLV childTLV = new ChildTLV();
		
		children.add(childTLV);
		sample.setChildren(children);

		byte[] bytes = ByteUtil.union(beanEncoder.encode(sample, beanEncoder.getEncodeContextFactory().createEncodeContext(SampleTLV.class, null)));

		System.out.println("TLV bytes is :\n" + ByteUtil.bytesAsHexString(bytes, 1024));

		Object bean = beanDecoder.decode(bytes.length, bytes, beanDecoder.getDecodeContextFactory().createDecodeContext(SampleTLV.class, null));
		System.out.println(bean);
	}
}
