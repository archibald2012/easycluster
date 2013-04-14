package org.easycluster.easycluster.cluster.common;

import java.util.Arrays;

public class StringUtil {

	public static boolean isBlank(String str) {
		int length;

		if ((str == null) || ((length = str.length()) == 0)) {
			return true;
		}

		for (int i = 0; i < length; i++) {
			if (!Character.isWhitespace(str.charAt(i))) {
				return false;
			}
		}

		return true;
	}

	public static boolean isEmpty(String str) {
		return ((str == null) || (str.length() == 0));
	}

	public static String rightPad(String text, int length, char c) {
		if (text == null) {
			return text;
		}
		if (text.length() >= length) {
			return text;
		}

		char[] array = new char[length];
		System.arraycopy(text.toCharArray(), 0, array, 0, text.length());
		Arrays.fill(array, text.length(), length, c);
		return new String(array);
	}

	public static String byte2Hex(byte hash[]) {
		StringBuffer buf = new StringBuffer(hash.length * 2);
		int i;

		for (i = 0; i < hash.length; i++) {
			if (((int) hash[i] & 0xff) < 0x10) {
				buf.append("0");
			}
			buf.append(Long.toString((int) hash[i] & 0xff, 16));
		}
		return buf.toString();
	}

	public static byte[] hex2byte(byte[] b) {

		if ((b.length % 2) != 0)
			throw new IllegalArgumentException("长度不是偶数");

		byte[] b2 = new byte[b.length / 2];
		for (int n = 0; n < b.length; n += 2) {
			String item = new String(b, n, 2);
			b2[n / 2] = (byte) Integer.parseInt(item, 16);
		}
		return b2;
	}

}
