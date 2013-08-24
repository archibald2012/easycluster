
package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import junit.framework.Assert;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.BooleanCodec;
import org.easycluster.easycluster.serialization.bytebean.context.DecResult;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultDecContextFactory;
import org.easycluster.easycluster.serialization.bytebean.context.DefaultEncContextFactory;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;


/**
 * TODO
 * 
 * @author wangqi
 * @version $Id: BooleanCodecTestCase.java 14 2012-01-10 11:54:14Z archie $
 */
public class BooleanCodecTestCase {

	private BooleanCodec codec;

	private DefaultEncContextFactory encContextFactory;
	private DefaultDecContextFactory decContextFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DefaultCodecProvider codecProvider = new DefaultCodecProvider();

		// 初始化解码器集合
		codec = new BooleanCodec();
		codecProvider.addCodec(codec);

		encContextFactory = new DefaultEncContextFactory();
		decContextFactory = new DefaultDecContextFactory();

		encContextFactory.setCodecProvider(codecProvider);
		encContextFactory.setNumberCodec(DefaultNumberCodecs
				.getBigEndianNumberCodec());

		decContextFactory.setCodecProvider(codecProvider);
		decContextFactory.setNumberCodec(DefaultNumberCodecs
				.getBigEndianNumberCodec());
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		codec = null;
		encContextFactory = null;
		decContextFactory = null;
	}

	@Test
	public void testEncode() {

		byte[] assertObj = codec.encode(encContextFactory.createEncContext(
				true, boolean.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		DecResult result = codec.decode(decContextFactory.createDecContext(
				assertObj, boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertTrue((Boolean) result.getValue());

		assertObj = codec.encode(encContextFactory.createEncContext(false,
				boolean.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		result = codec.decode(decContextFactory.createDecContext(assertObj,
				boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertFalse((Boolean) result.getValue());
	}
	
	@Test
	public void testDecode() {

		DecResult result = codec.decode(decContextFactory.createDecContext(
				new byte[] { 0 }, boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertFalse((Boolean) result.getValue());

		result = codec.decode(decContextFactory.createDecContext(
				new byte[] { 2 }, boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertTrue((Boolean) result.getValue());

		result = codec.decode(decContextFactory.createDecContext(new byte[] {
				2, 3 }, boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertTrue((Boolean) result.getValue());

		result = codec.decode(decContextFactory.createDecContext(
				new byte[] { -1 }, boolean.class, null, null));
		System.out.println(result.getValue());
		Assert.assertTrue((Boolean) result.getValue());

	}
}
