package org.easycluster.easycluster.cluster.netty.websocket;

import org.easycluster.easycluster.cluster.netty.response.DefaultHttpResponseSender;
import org.easycluster.easycluster.cluster.netty.response.HttpResponseSender;
import org.jboss.netty.channel.ChannelHandlerContext;
import org.jboss.netty.channel.MessageEvent;
import org.jboss.netty.channel.SimpleChannelUpstreamHandler;
import org.jboss.netty.handler.codec.http.HttpHeaders;
import org.jboss.netty.handler.codec.http.HttpMethod;
import org.jboss.netty.handler.codec.http.HttpRequest;
import org.jboss.netty.handler.codec.http.HttpResponseStatus;
import org.jboss.netty.handler.codec.http.websocketx.CloseWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PingWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.PongWebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketFrame;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshaker;
import org.jboss.netty.handler.codec.http.websocketx.WebSocketServerHandshakerFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class WebSocketServerHandshakerHandler extends SimpleChannelUpstreamHandler {

	@SuppressWarnings("unused")
	private static final Logger					LOGGER			= LoggerFactory.getLogger(WebSocketServerHandshakerHandler.class);

	private static final String					WEBSOCKET_PATH	= "/websocket";
	private HttpResponseSender					responseSender	= new DefaultHttpResponseSender();

	private volatile WebSocketServerHandshaker	handshaker;

	@Override
	public void messageReceived(ChannelHandlerContext ctx, MessageEvent e) throws Exception {
		Object msg = e.getMessage();
		if (msg instanceof HttpRequest) {
			handleHttpRequest(ctx, (HttpRequest) msg);
		} else if (msg instanceof CloseWebSocketFrame) {
			handshaker.close(ctx.getChannel(), (CloseWebSocketFrame) msg);
		} else if (msg instanceof PingWebSocketFrame) {
			ctx.getChannel().write(new PongWebSocketFrame(((WebSocketFrame) msg).getBinaryData()));
		}
	}

	private void handleHttpRequest(ChannelHandlerContext ctx, HttpRequest req) throws Exception {
		// Allow only GET methods.
		if (req.getMethod() != HttpMethod.GET) {
			responseSender.sendResponse(ctx.getChannel(), HttpResponseStatus.FORBIDDEN);
			return;
		}

		// Send the demo page and favicon.ico
		if (req.getUri().equals("/")) {
			responseSender.sendResponse(ctx.getChannel(), HttpResponseStatus.OK, WebSocketServerIndexPage.getContent(getWebSocketLocation(req)), "UTF-8",
					"text/html; charset=UTF-8");
			return;
		} else if (req.getUri().equals("/favicon.ico")) {
			responseSender.sendResponse(ctx.getChannel(), HttpResponseStatus.NOT_FOUND);
			return;
		}

		// Handshake
		WebSocketServerHandshakerFactory wsFactory = new WebSocketServerHandshakerFactory(getWebSocketLocation(req), null, false);
		handshaker = wsFactory.newHandshaker(req);
		if (handshaker == null) {
			wsFactory.sendUnsupportedWebSocketVersionResponse(ctx.getChannel());
		} else {
			handshaker.handshake(ctx.getChannel(), req).addListener(WebSocketServerHandshaker.HANDSHAKE_LISTENER);
		}
	}

	private String getWebSocketLocation(HttpRequest req) {
		return "ws://" + req.getHeader(HttpHeaders.Names.HOST) + WEBSOCKET_PATH;
	}
}

final class WebSocketServerIndexPage {

	private static final String	NEWLINE	= "\r\n";

	public static String getContent(String webSocketLocation) {
		return "<html><head><title>Web Socket Index Page</title></head>" + NEWLINE + "<body>" + NEWLINE + "<script type=\"text/javascript\">" + NEWLINE
				+ "var socket;" + NEWLINE + "if (!window.WebSocket) {" + NEWLINE + "  window.WebSocket = window.MozWebSocket;" + NEWLINE + "}" + NEWLINE
				+ "if (window.WebSocket) {" + NEWLINE + "  socket = new WebSocket(\"" + webSocketLocation + "\");" + NEWLINE
				+ "  socket.onmessage = function(event) {" + NEWLINE + "    var ta = document.getElementById('responseText');" + NEWLINE
				+ "    ta.value = ta.value + '\\n' + event.data" + NEWLINE + "  };" + NEWLINE + "  socket.onopen = function(event) {" + NEWLINE
				+ "    var ta = document.getElementById('responseText');" + NEWLINE + "    ta.value = \"Web Socket opened!\";" + NEWLINE + "  };" + NEWLINE
				+ "  socket.onclose = function(event) {" + NEWLINE + "    var ta = document.getElementById('responseText');" + NEWLINE
				+ "    ta.value = ta.value + \"Web Socket closed\"; " + NEWLINE + "  };" + NEWLINE + "} else {" + NEWLINE
				+ "  alert(\"Your browser does not support Web Socket.\");" + NEWLINE + "}" + NEWLINE + NEWLINE + "function send(message) {" + NEWLINE
				+ "  if (!window.WebSocket) { return; }" + NEWLINE + "  if (socket.readyState == WebSocket.OPEN) {" + NEWLINE + "    socket.send(message);"
				+ NEWLINE + "  } else {" + NEWLINE + "    alert(\"The socket is not open.\");" + NEWLINE + "  }" + NEWLINE + "}" + NEWLINE + "</script>"
				+ NEWLINE + "<form onsubmit=\"return false;\">" + NEWLINE + "<input type=\"text\" name=\"message\" value=\"Hello, World!\"/>"
				+ "<input type=\"button\" value=\"Send Web Socket Data\"" + NEWLINE + "       onclick=\"send(this.form.message.value)\" />" + NEWLINE
				+ "<h3>Output</h3>" + NEWLINE + "<textarea id=\"responseText\" style=\"width:500px;height:300px;\"></textarea>" + NEWLINE + "</form>" + NEWLINE
				+ "</body>" + NEWLINE + "</html>" + NEWLINE;
	}

	private WebSocketServerIndexPage() {
		// Unused
	}
}
