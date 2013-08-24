
package org.easycluster.easycluster.serialization.bytebean.codec.primitive;

import junit.framework.Assert;

import org.easycluster.easycluster.serialization.bytebean.codec.DefaultCodecProvider;
import org.easycluster.easycluster.serialization.bytebean.codec.DefaultNumberCodecs;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.LenStringCodec;
import org.easycluster.easycluster.serialization.bytebean.codec.primitive.ShortCodec;
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
public class LenStringCodecTestCase {

	private LenStringCodec						codec;

	private DefaultEncContextFactory	encContextFactory;
	private DefaultDecContextFactory	decContextFactory;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		DefaultCodecProvider codecProvider = new DefaultCodecProvider();

		// 初始化解码器集合
		codec = new LenStringCodec();
		codecProvider.addCodec(codec);
		codecProvider.addCodec(new ShortCodec());

		encContextFactory = new DefaultEncContextFactory();
		decContextFactory = new DefaultDecContextFactory();

		encContextFactory.setCodecProvider(codecProvider);
		encContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());

		decContextFactory.setCodecProvider(codecProvider);
		decContextFactory.setNumberCodec(DefaultNumberCodecs.getBigEndianNumberCodec());
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

		String source = "是否强制弹出是否强制弹出是否强制弹出是否强制弹出是否强制弹出";
		byte[] assertObj = codec.encode(encContextFactory.createEncContext(source, String.class, null));
		
		DecResult result = codec.decode(decContextFactory.createDecContext(assertObj, String.class, null, null));
		System.out.println(result.getValue());
		Assert.assertEquals(source, result.getValue());
	}

}
