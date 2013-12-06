package org.easycluster.easycluster.websocket;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.util.CharsetUtil;

public class WebSocketHeaderUtil {

	public static String readBody(ChannelBuffer buffer) {
		return buffer.toString(buffer.readerIndex(), buffer.readableBytes(), CharsetUtil.UTF_8);
	}

	public static Map<String, String> parseHeader(ChannelBuffer buffer) {
		Map<String, String> header = new HashMap<String, String>();
		int preIndex = 0;
		while (!Thread.interrupted()) {
			int index = buffer.indexOf(preIndex, buffer.readableBytes(), (byte) '\n');
			if (index < 0) {
				break;
			}
			if (index - preIndex <= 1) {
				// header is end
				buffer.readerIndex(index + 1);
				break;
			}
			byte[] bytes = new byte[index - preIndex];
			buffer.getBytes(preIndex, bytes);
			int splitIndex = binarySearch(bytes, (byte) ':');
			if (splitIndex < 0) {
				break;
			}
			String key = decode(new String(bytes, 0, splitIndex, CharsetUtil.UTF_8));
			String value = null;
			if (bytes[bytes.length - 1] == (byte) ('\r')) {
				value = decode(new String(bytes, splitIndex + 1, bytes.length - splitIndex - 2, CharsetUtil.UTF_8));
			} else {
				value = decode(new String(bytes, splitIndex + 1, bytes.length - splitIndex - 1, CharsetUtil.UTF_8));
			}
			header.put(key, value);
			preIndex = index + 1;
		}
		return header;
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, CharsetUtil.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, CharsetUtil.UTF_8.name());
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	private static int binarySearch(byte[] bytes, byte b) {
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == b) {
				return i;
			}
		}
		return -1;
	}

}
