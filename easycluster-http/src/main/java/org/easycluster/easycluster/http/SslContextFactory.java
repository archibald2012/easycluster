package org.easycluster.easycluster.http;

import java.io.File;
import java.io.FileInputStream;
import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.Security;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

import org.apache.commons.lang.StringUtils;
import org.easycluster.easycluster.cluster.SslConfig;

public class SslContextFactory {

	private final SSLContext	_serverContext;
	private final SSLContext	_clientContext;

	public SslContextFactory(SslConfig sslConfig) {

		SSLContext serverContext = null;
		try {
			// Key store (Server side certificate)
			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			if (algorithm == null) {
				algorithm = "SunX509";
			}

			KeyManager[] km = null;

			String keyStoreFilePath = sslConfig.getKeyStore();
			String keyStoreFilePassword = sslConfig.getKeyStorePassword();
			if (StringUtils.isNotBlank(keyStoreFilePath) && StringUtils.isNotBlank(keyStoreFilePassword)) {
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream(new File(keyStoreFilePath)), keyStoreFilePassword.toCharArray());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
				kmf.init(ks, keyStoreFilePassword.toCharArray());
				km = kmf.getKeyManagers();
			}

			//TrustManager[] tm = null;
			
			// String trustStoreFilePath = sslConfig.getTrustStore();
			// String trustStoreFilePassword =
			// sslConfig.getTrustStorePassword();
			// if (StringUtils.isNotBlank(trustStoreFilePath) &&
			// StringUtils.isNotBlank(trustStoreFilePassword)) {
			// KeyStore trustStore = KeyStore.getInstance("JKS");
			// trustStore.load(new FileInputStream(new
			// File(trustStoreFilePath)), trustStoreFilePassword.toCharArray());
			// TrustManagerFactory tmf =
			// TrustManagerFactory.getInstance("SunX509");
			// tmf.init(trustStore);
			// tm = tmf.getTrustManagers();
			// }

			// Initialise the SSLContext to work with our key managers.
			serverContext = SSLContext.getInstance(sslConfig.getSslProtocol());
			serverContext.init(km, null, null);

		} catch (Exception ex) {
			throw new Error("Failed to initialize the server-side SSLContext. " + ex.getMessage(), ex);
		} finally {
			_serverContext = serverContext;
		}

		SSLContext clientContext = null;
		try {
			clientContext = SSLContext.getInstance(sslConfig.getSslProtocol());
			clientContext.init(null, TrustManagerFactory.getTrustManagers(), null);

		} catch (Exception ex) {
			throw new Error("Failed to initialize the client-side SSLContext. " + ex.getMessage(), ex);
		} finally {
			_clientContext = clientContext;
		}
	}

	public SSLContext getServerContext() {
		return _serverContext;
	}

	public SSLContext getClientContext() {
		return _clientContext;
	}
}

class TrustManagerFactory extends TrustManagerFactorySpi {

	private static final TrustManager	DUMMY_TRUST_MANAGER	= new X509TrustManager() {
																public X509Certificate[] getAcceptedIssuers() {
																	return new X509Certificate[0];
																}

																public void checkClientTrusted(X509Certificate[] chain, String authType)
																		throws CertificateException {

																	System.err.println("UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
																}

																public void checkServerTrusted(X509Certificate[] chain, String authType)
																		throws CertificateException {

																	System.err.println("UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
																}
															};

	public static TrustManager[] getTrustManagers() {
		return new TrustManager[] { DUMMY_TRUST_MANAGER };
	}

	@Override
	protected TrustManager[] engineGetTrustManagers() {
		return getTrustManagers();
	}

	@Override
	protected void engineInit(KeyStore keystore) throws KeyStoreException {
		// Unused
	}

	@Override
	protected void engineInit(ManagerFactoryParameters managerFactoryParameters) throws InvalidAlgorithmParameterException {
		// Unused
	}
}
