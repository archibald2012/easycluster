package org.easycluster.easycluster.monitor;

import java.io.File;

import org.apache.catalina.LifecycleException;
import org.apache.catalina.core.AprLifecycleListener;
import org.apache.catalina.core.StandardServer;
import org.apache.catalina.startup.Tomcat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EmbbedTomcat {

	private static final Logger	logger		= LoggerFactory.getLogger(EmbbedTomcat.class);

	private String				hostname	= "localhost";
	private int					port		= 18080;
	private String				webAppDir	= "webapp";
	private String				contextPath	= "/";
	private String				URIEncoding	= "UTF-8";

	private String				baseDir		= ".";

	// absolute pathname, a relative pathname, or a URL
	private String				appBase		= ".";

	private Tomcat				tomcat		= null;

	public String getHostname() {
		return hostname;
	}

	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

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

	public String getBaseDir() {
		return baseDir;
	}

	public void setBaseDir(String baseDir) {
		this.baseDir = baseDir;
	}

	public String getAppBase() {
		return appBase;
	}

	public void setAppBase(String appBase) {
		this.appBase = appBase;
	}

	public void setURIEncoding(String uRIEncoding) {
		URIEncoding = uRIEncoding;
	}

	public void start() throws Exception {

		tomcat = new Tomcat();

		tomcat.setPort(port);
		tomcat.setHostname(hostname);
		tomcat.setSilent(false);
		tomcat.setBaseDir(baseDir);
		tomcat.getConnector().setURIEncoding(URIEncoding);

		// webapps
		tomcat.getHost().setAppBase(System.getProperty("user.dir") + File.separator + appBase);

		StandardServer server = (StandardServer) tomcat.getServer();
		AprLifecycleListener listener = new AprLifecycleListener();
		server.addLifecycleListener(listener);

		// ROOT
		tomcat.addWebapp(contextPath, webAppDir);

		tomcat.start();

		// add shutdown hook to stop server
		Runtime.getRuntime().addShutdownHook(new Thread() {
			public void run() {
				try {
					tomcat.stop();
				} catch (LifecycleException e) {
					logger.error("failed to stop tomcat.", e);
				}
			}
		});

		tomcat.getServer().await();
	}

	public static void main(String[] args) {

		int port = 0;
		String appBase = null;
		String contextPath = null;
		String webAppDir = null;
		String baseDir = null;
		String URIEncoding = null;
		for (String arg : args) {
			if (arg.startsWith("-httpPort")) {
				port = Integer.parseInt(arg.substring(arg.indexOf("=") + 1));
			}
			if (arg.startsWith("-appBase")) {
				appBase = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-contextPath")) {
				contextPath = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-webAppDir")) {
				webAppDir = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-baseDir")) {
				baseDir = arg.substring(arg.indexOf("=") + 1);
			}
			if (arg.startsWith("-URIEncoding")) {
				URIEncoding = arg.substring(arg.indexOf("=") + 1);
			}
		}

		EmbbedTomcat tomcat = new EmbbedTomcat();
		if (port > 0) {
			tomcat.setPort(port);
		}
		if (appBase != null && appBase.length() > 0) {
			tomcat.setAppBase(appBase);
		}
		if (contextPath != null && contextPath.length() > 0) {
			tomcat.setContextPath(contextPath);
		}
		if (webAppDir != null && webAppDir.length() > 0) {
			tomcat.setWebAppDir(webAppDir);
		}
		if (baseDir != null && baseDir.length() > 0) {
			tomcat.setBaseDir(baseDir);
		}
		if (URIEncoding != null && URIEncoding.length() > 0) {
			tomcat.setURIEncoding(URIEncoding);
		}

		try {
			tomcat.start();
		} catch (Exception e) {
			logger.error("Server Start Error: ", e);
			System.exit(-1);
		}

	}

}
