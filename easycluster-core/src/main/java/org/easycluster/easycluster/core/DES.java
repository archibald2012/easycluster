/**
 * 
 */
package org.easycluster.easycluster.core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

import org.apache.commons.lang.ArrayUtils;

/**
 * @author Archibald.Wang
 */
public class DES {

	private static final String DEFAULT_CHARSET = "UTF-8";
	private static final String ALGORITHM = "DESede";

	public static byte[] encryptThreeDESECB(byte[] src, byte[] key)
			throws Exception {

		DESedeKeySpec dks = new DESedeKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey securekey = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance(ALGORITHM);

		cipher.init(Cipher.ENCRYPT_MODE, securekey);

		byte[] b = cipher.doFinal(src);

		return b;

	}

	public static byte[] decryptThreeDESECB(byte[] src, byte[] key)
			throws Exception {

		DESedeKeySpec dks = new DESedeKeySpec(key);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

		SecretKey securekey = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance(ALGORITHM);

		cipher.init(Cipher.DECRYPT_MODE, securekey);

		byte[] retByte = cipher.doFinal(src);

		return retByte;
	}

	public static void main(String[] argv) throws Exception {

		String source = "Don't tell anybody!";
		byte[] key = "izmFNROXQ98C3w3T8tTiDD/ril0TlAzJGuEY+WiagsN19YPz3ewZJPIsLH4JBp2mUDwr5mfv1y7mPNLAnnndSQbklhcpK/aZXJfO7xxuLt2Z9/xRX7J6DcxlHa9LTOfhloXHrlFeOVGbX1O8Et4t6DvFdZOh2SIendLNsF"
				.getBytes(DEFAULT_CHARSET);
		System.out.println(key.length);
		System.out.println("key: " + ArrayUtils.toString(key));

		byte[] encryptedString = DES.encryptThreeDESECB(source.getBytes(), key);
		byte[] decryptedString = DES.decryptThreeDESECB(encryptedString, key);

		System.out.println("source:" + source);
		System.out.println("encryptedString:" + new String(encryptedString));
		System.out.println("decryptedString:" + new String(decryptedString));

	}
}
