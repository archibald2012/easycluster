/**
 * 
 */
package org.easycluster.easycluster.serialization.tlv.encode.encoders;

import java.lang.reflect.Field;
import java.util.List;

import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.NumberCodec;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncodeContext;
import org.easycluster.easycluster.serialization.tlv.encode.TLVEncoderRepository;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Marvin.Ma
 * 
 */
public class LongTLVEncoderTestCase {

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void testLongTLVEncoderAnnoBytesAware() {
		final TestLongEncoderBean testBean = new TestLongEncoderBean();

		testBean.setMyLong(100);

		LongTLVEncoder encoder = new LongTLVEncoder();

		TLVEncodeContext ctx = new TLVEncodeContext() {

			public TLVEncoderRepository getEncoderRepository() {
				return null;
			}

			public NumberCodec getNumberCodec() {
				return DefaultNumberCodecs.getBigEndianNumberCodec();
			}

			public Class<?> getValueType() {
				return Long.class;
			}

			public Field getValueField() {
				try {
					return testBean.getClass().getDeclaredField("myLong");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		List<byte[]> ret = encoder.encode(testBean.getMyLong(), ctx);

		Assert.assertEquals(ret.size(), 1);
		Assert.assertEquals(ret.get(0).length, 4);
		Assert.assertEquals(ret.get(0)[0], 0);
		Assert.assertEquals(ret.get(0)[1], 0);
		Assert.assertEquals(ret.get(0)[2], 0);
		Assert.assertEquals(ret.get(0)[3], 100);
	}

	@Test
	public void testLongTLVEncoderNoBytesAware() {
		final TestLongEncoderBean testBean = new TestLongEncoderBean();

		testBean.setMyLongNoAnnoBytes(1000); // 0x3E8

		LongTLVEncoder encoder = new LongTLVEncoder();

		TLVEncodeContext ctx = new TLVEncodeContext() {

			public TLVEncoderRepository getEncoderRepository() {
				return null;
			}

			public NumberCodec getNumberCodec() {
				return DefaultNumberCodecs.getBigEndianNumberCodec();
			}

			public Class<?> getValueType() {
				return Long.class;
			}

			public Field getValueField() {
				try {
					return testBean.getClass().getDeclaredField("myLongNoAnnoBytes");
				} catch (Exception e) {
					throw new RuntimeException(e);
				}
			}
		};
		List<byte[]> ret = encoder.encode(testBean.getMyLongNoAnnoBytes(), ctx);

		Assert.assertEquals(ret.size(), 1);
		Assert.assertEquals(ret.get(0).length, 8);
		Assert.assertEquals(ret.get(0)[0], 0);
		Assert.assertEquals(ret.get(0)[1], 0);
		Assert.assertEquals(ret.get(0)[2], 0);
		Assert.assertEquals(ret.get(0)[3], 0);
		Assert.assertEquals(ret.get(0)[4], 0);
		Assert.assertEquals(ret.get(0)[5], 0);
		Assert.assertEquals(ret.get(0)[6], (byte) 0x3);
		Assert.assertEquals(ret.get(0)[7], (byte) 0xE8);
	}
}
