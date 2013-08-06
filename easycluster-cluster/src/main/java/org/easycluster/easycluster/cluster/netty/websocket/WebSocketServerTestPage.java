package org.easycluster.easycluster.cluster.netty.websocket;

import org.jboss.netty.buffer.ChannelBuffer;
import org.jboss.netty.buffer.ChannelBuffers;
import org.jboss.netty.util.CharsetUtil;

public final class WebSocketServerTestPage {

	private static final String NEWLINE = "\r\n";

	public static ChannelBuffer getContent(String webSocketLocation) {
		return ChannelBuffers
				.copiedBuffer(
						"<html><head><title>Web Socket Test</title></head>"
								+ NEWLINE
								+ "<body>"
								+ NEWLINE
								+ "<script type=\"text/javascript\">"
								+ NEWLINE
								+ "var socket;"
								+ NEWLINE
								+ "function open(){if (!window.WebSocket) {"
								+ NEWLINE
								+ "  window.WebSocket = window.MozWebSocket;"
								+ NEWLINE
								+ '}'
								+ NEWLINE
								+ "if (window.WebSocket) {"
								+ NEWLINE
								+ "  socket = new WebSocket(\""
								+ webSocketLocation
								+ "\");"
								+ NEWLINE
								+ "  socket.onmessage = function(event) {"
								+ NEWLINE
								+ "    var ta = document.getElementById('responseText');"
								+ NEWLINE
								+ "    ta.value = ta.value + '\\n' + event.data"
								+ NEWLINE
								+ "  };"
								+ NEWLINE
								+ "  socket.onopen = function(event) {"
								+ NEWLINE
								+ " alert('Web Socket  open!');"
								+ NEWLINE
								+ "  };"
								+ NEWLINE
								+ "  socket.onclose = function(event) {"
								+ NEWLINE
								+ " var ta = document.getElementById('responseText');" +
								" ta.value = ta.value + '\\r\\n' +'Web Socket closed!\\r\\nretry to open!';" +
								"open(); "
								+ NEWLINE
								+ " };"
								+ NEWLINE
								+ "} else {"
								+ NEWLINE
								+ "  alert(\"Your browser does not support Web Socket.\");"
								+ NEWLINE
								+ '}'
								+ NEWLINE
								+ NEWLINE
								+ "}" +NEWLINE+
								"open();function send(message) {"
								+ NEWLINE
								+ "  if (!window.WebSocket) { return; }"
								+ NEWLINE
								+ "if(!socket){return ;}" +
								"" +
								"  if (socket.readyState == WebSocket.OPEN) {"
								+ NEWLINE
								+ "    socket.send(message);"
								+ NEWLINE
								+ "  } else {"
								+ NEWLINE
								+ "    alert(\"The socket is not open.\");"
								+ NEWLINE
								+ "  }"
								+ NEWLINE
								+ '}'
								+ NEWLINE
								+ "</script>"
								+ NEWLINE
								+ "<form onsubmit=\"return false;\">"
								+ NEWLINE
								+ "<input type=\"button\" value=\"Send Web Socket Data\""
								+ NEWLINE
								+ "  onclick=\"send(this.form.requestText.value)\" /><br></br>"
								+ NEWLINE
								+ "<table><tr><td>input</td><td>output</td></tr>"
								+ NEWLINE
								+ "<tr><td><textarea id=\"requestText\" style=\"width:500px;height:300px;\">cmd:send\r\n" +
								"dest:%2Bxiaosong\r\n" +
								"ack:1\r\n" +
								"sid:msgId123456\r\n" +
								"t:im\r\n\r\n" +
								"{key:'test'}</textarea></td>"
								+ NEWLINE
								+ "<td><textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" +
								"<input type=\"button\" value='clear' onclick=\"this.form.responseText.value='';\"/>" +
								"</td>"
								+ NEWLINE + "</tr></table></form>" + NEWLINE + "</body>"
								+ NEWLINE + "</html>" + NEWLINE,
						CharsetUtil.US_ASCII);
	}

}
