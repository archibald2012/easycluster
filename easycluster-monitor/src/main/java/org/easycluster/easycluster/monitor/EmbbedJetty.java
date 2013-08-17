package org.easycluster.easycluster.monitor;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.webapp.WebAppContext;

public class EmbbedJetty {

	private int		port		= 10000;
	private String	webAppDir	= "webapp";
	private String	contextPath	= "/";

	private Server	server		= null;

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getWebAppDir() {
		return webAppDir;
	}

	public void setWebAppDir(String webAppDir) {
		this.webAppDir = webAppDir;
	}

	public String getContextPath() {
		return contextPath;
	}

	public void setContextPath(String contextPath) {
		this.contextPath = contextPath;
	}

	public void start() throws Exception {

		server = new Server(port);

		WebAppContext context = new WebAppContext();
		context.setContextPath(contextPath);
		context.setDescriptor(webAppDir + "/WEB-INF/web.xml");
		context.setResourceBase(webAppDir);
		context.setClassLoader(Thread.currentThread().getContextClassLoader());  
		server.setHandler(context);

		server.start();
		server.join();
	}

	public static void main(String[] args) throws Exception {

		int port = 0;
		String contextPath = null;
		String webAppDir = null;
		for (String arg : args) {
			if (arg.startsWith("-httpPort")) {
				port = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
			}
			if (arg.startsWith("-contextPath")) {
				contextPath = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-webAppDir")) {
				webAppDir = arg.substring(arg.indexOf("=") + 1);
			}
		}

		EmbbedJetty tomcat = new EmbbedJetty();
		if (port > 0) {
			tomcat.setPort(port);
		}
		if (contextPath != null && contextPath.length() > 0) {
			tomcat.setContextPath(contextPath);
		}
		if (webAppDir != null && webAppDir.length() > 0) {
			tomcat.setWebAppDir(webAppDir);
		}

		tomcat.start();
	}

}
