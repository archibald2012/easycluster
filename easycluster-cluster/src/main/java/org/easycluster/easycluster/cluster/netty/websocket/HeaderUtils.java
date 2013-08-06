package org.easycluster.easycluster.cluster.netty.websocket;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.jboss.netty.buffer.ChannelBuffer;

public class HeaderUtils {

	public static final String ENCODEING = "UTF-8";

	public static final Charset UTF_8_CHARSET = Charset.forName(ENCODEING);

	public static final String CMD = "cmd";

	public static final String CMD_SEND = "send";

	public static final String CMD_ACK = "ack";

	public static final String CMD_AUTH = "auth";

	public static final String CMD_INVOKE = "invoke";

	public static final String UID = "uid";

	public static final String SID = "sid";

	public static final String DEST = "dest";

	public static final String ACK_0 = "0";

	public static final String ACK_1 = "1";

	public static final String TYPE = "t";

	public static final String TYPE_API = "api";

	public static final String AT = "at";

	public static final String TIME = "time";

	public static final String HEARTBEAT_INTERVAL = "hbi";

	public static final String CODE = "code";

	public static final String SN = "sn";

	public static final String AUTH_MTOP = "auth_mtop";

	public static final String AUTH_TMALL = "auth_tmall";

	public static Map<String, String> parseHeader(ChannelBuffer buffer) {
		Map<String, String> header = new HashMap<String, String>();
		int preIndex = 0;
		while (!Thread.interrupted()) {
			int index = buffer.indexOf(preIndex, buffer.readableBytes(),
					(byte) '\n');
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
			String key = decode(new String(bytes, 0, splitIndex, UTF_8_CHARSET));
			String value = null;
			if (bytes[bytes.length - 1] == (byte) ('\r')) {
				value = decode(new String(bytes, splitIndex + 1, bytes.length
						- splitIndex - 2, UTF_8_CHARSET));
			} else {
				value = decode(new String(bytes, splitIndex + 1, bytes.length
						- splitIndex - 1, UTF_8_CHARSET));
			}
			header.put(key, value);
			preIndex = index + 1;
		}
		return header;
	}

	public static Map<String, String> parseBody(ChannelBuffer buffer) {
		Map<String, String> map = new HashMap<String, String>();
		String body = readBody(buffer);
		if (body.length() > 0 && Character.isLetter(body.charAt(0))) {
			String[] entries = body.split("\r\n");
			for (String entry : entries) {
				int idx = entry.indexOf(':');
				if (idx > 0) {
					map.put(entry.substring(0, idx).trim(), 
							entry.substring(idx + 1).trim());
				}
			}
		} else {
			map.put("data", body);
		}
		return map;
	}

	public static String readBody(ChannelBuffer buffer) {
		return buffer.toString(buffer.readerIndex(), buffer.readableBytes(),
				UTF_8_CHARSET);
	}

	public static StringBuilder buildHeader(Map<String, String> headers) {
		Set<Map.Entry<String, String>> set = headers.entrySet();
		StringBuilder sb = new StringBuilder(40);
		for (Entry<String, String> entry : set) {
			String key = entry.getKey();
			String value = entry.getValue();
			sb.append(HeaderUtils.encode(key)).append(":")
					.append(HeaderUtils.encode(value)).append("\r\n");
		}
		return sb.append("\r\n");
	}

	private static int binarySearch(byte[] bytes, byte b) {
		for (int i = 0; i < bytes.length; i++) {
			if (bytes[i] == b) {
				return i;
			}
		}
		return -1;
	}

	public static String decode(String s) {
		try {
			return URLDecoder.decode(s, HeaderUtils.ENCODEING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}

	public static String encode(String s) {
		try {
			return URLEncoder.encode(s, HeaderUtils.ENCODEING);
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException(e);
		}
	}
}
