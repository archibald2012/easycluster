/*******************************************************************************
 * CopyRight (c) 2005-2011 TAOTAOSOU Co, Ltd. All rights reserved.
 * Filename:    FloatCodecTestCase.java
 * Creator:     wangqi
 * Create-Date: 2011-7-13 上午10:36:10
 *******************************************************************************/
package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import junit.framework.Assert;

import org.apache.commons.lang.ArrayUtils;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.DoubleCodec;
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
 * @version $Id: DoubleCodecTestCase.java 14 2012-01-10 11:54:14Z archie $
 */
public class DoubleCodecTestCase {

	private DoubleCodec codec;

	private DefaultEncContextFactory encContextFactory;
	private DefaultDecContextFactory decContextFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DefaultCodecProvider codecProvider = new DefaultCodecProvider();

		// 初始化解码器集合
		codec = new DoubleCodec();
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
	public void testCodec() {

		byte[] assertObj = codec.encode(encContextFactory.createEncContext(
				1.2d, double.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		DecResult result = codec.decode(decContextFactory.createDecContext(
				assertObj, double.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(1.2d, (Double) result.getValue());

		assertObj = codec.encode(encContextFactory.createEncContext(-1.2d,
				double.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		result = codec.decode(decContextFactory.createDecContext(assertObj,
				double.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(-1.2d, (Double) result.getValue());

		assertObj = codec.encode(encContextFactory.createEncContext(0d,
				double.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		result = codec.decode(decContextFactory.createDecContext(assertObj,
				double.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(0d, (Double) result.getValue());

		assertObj = codec.encode(encContextFactory.createEncContext(1d / 3,
				double.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		result = codec.decode(decContextFactory.createDecContext(assertObj,
				double.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(1d / 3, (Double) result.getValue());

		assertObj = codec.encode(encContextFactory.createEncContext(
				0.000000000000001d, double.class, null));
		System.out.println(ArrayUtils.toString(assertObj));
		result = codec.decode(decContextFactory.createDecContext(assertObj,
				double.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(0.000000000000001d, (Double) result.getValue());
	}
}
