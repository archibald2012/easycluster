/**
 * 
 */
package org.easycluster.easycluster.core;

import javax.crypto.Cipher;
import javax.crypto.SecretKey;
import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.DESedeKeySpec;

/**
 * @author Archibald.Wang
 */
public class DES {

	private static final String	ALGORITHM	= "DESede";

	public static byte[] encryptThreeDESECB(byte[] src, byte[] key) throws Exception {

		DESedeKeySpec dks = new DESedeKeySpec(key);
		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);
		SecretKey securekey = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance(ALGORITHM);

		cipher.init(Cipher.ENCRYPT_MODE, securekey);

		byte[] b = cipher.doFinal(src);

		return b;

	}

	public static byte[] decryptThreeDESECB(byte[] src, byte[] key) throws Exception {

		DESedeKeySpec dks = new DESedeKeySpec(key);

		SecretKeyFactory keyFactory = SecretKeyFactory.getInstance(ALGORITHM);

		SecretKey securekey = keyFactory.generateSecret(dks);

		Cipher cipher = Cipher.getInstance(ALGORITHM);

		cipher.init(Cipher.DECRYPT_MODE, securekey);

		byte[] retByte = cipher.doFinal(src);

		return retByte;
	}

}
