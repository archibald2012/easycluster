package org.easycluster.easycluster.core;

import org.apache.commons.lang.ArrayUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DESTestCase {

	private static final String	DEFAULT_CHARSET	= "UTF-8";

	@Before
	public void setUp() throws Exception {
	}

	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void test() throws Exception {
		String source = "Don't tell anybody!";
		byte[] key = "izmFNROXQ98C3w3T8tTiDD/ril0TlAzJGuEY+WiagsN19YPz3ewZJPIsLH4JBp2mUDwr5mfv1y7mPNLAnnndSQbklhcpK/aZXJfO7xxuLt2Z9/xRX7J6DcxlHa9LTOfhloXHrlFeOVGbX1O8Et4t6DvFdZOh2SIendLNsF"
				.getBytes(DEFAULT_CHARSET);
		System.out.println(key.length);
		System.out.println("key: " + ArrayUtils.toString(key));

		byte[] encryptedString = DES.encryptThreeDESECB(source.getBytes("UTF-8"), key);
		byte[] decryptedString = DES.decryptThreeDESECB(encryptedString, key);

		System.out.println("source:" + source);
		System.out.println("encryptedString:" + new String(encryptedString, "UTF-8"));
		System.out.println("decryptedString:" + new String(decryptedString, "UTF-8"));
	}

}
