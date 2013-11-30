package org.easycluster.easycluster.cluster.ssl;

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
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SSLContextFactory {

	public SSLContext createSslContext(SSLConfig sslConfig) {

		SSLContext context = null;
		try {
			// Key store (Server side certificate)
			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			if (algorithm == null) {
				algorithm = "SunX509";
			}

			String keyStoreLocation = sslConfig.getKeyStore();
			String keyStorePassword = sslConfig.getKeyStorePassword();
			KeyStore ks = KeyStore.getInstance("JKS");
			ks.load(new FileInputStream(new File(keyStoreLocation)), keyStorePassword.toCharArray());
			KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
			kmf.init(ks, keyStorePassword.toCharArray());
			KeyManager[] keyManagers = kmf.getKeyManagers();

			// if it's two-way certification
			TrustManager[] trustManagers = null;
			if (sslConfig.getTrustStore() != null) {
				KeyStore trustStore = KeyStore.getInstance("JKS");
				trustStore.load(new FileInputStream(new File(sslConfig.getTrustStore())), sslConfig.getTrustStorePassword().toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
				tmf.init(trustStore);
				trustManagers = tmf.getTrustManagers();
			}

			// Initialise the SSLContext to work with our key managers.
			context = SSLContext.getInstance(sslConfig.getProtocol());
			context.init(keyManagers, trustManagers, null);
		} catch (Exception ex) {
			throw new Error("Failed to initialize the server-side SSLContext. " + ex.getMessage(), ex);
		}
		return context;
	}

	public SSLContext createDummySslContext(String protocol) {
		SSLContext context = null;
		try {
			context = SSLContext.getInstance(protocol);
			context.init(null, DummyTrustManagerFactory.getTrustManagers(), null);
		} catch (Exception ex) {
			throw new Error("Failed to initialize the client-side SSLContext. " + ex.getMessage(), ex);
		}
		return context;
	}
}

class DummyTrustManagerFactory extends TrustManagerFactorySpi {

	private static final Logger			LOGGER				= LoggerFactory.getLogger(DummyTrustManagerFactory.class);

	private static final TrustManager	DUMMY_TRUST_MANAGER	= new X509TrustManager() {
																public X509Certificate[] getAcceptedIssuers() {
																	return new X509Certificate[0];
																}

																public void checkClientTrusted(X509Certificate[] chain, String authType)
																		throws CertificateException {

																	LOGGER.info("CHECK CLIENT CERTIFICATE: {}", chain[0].getSubjectDN());
																}

																public void checkServerTrusted(X509Certificate[] chain, String authType)
																		throws CertificateException {

																	LOGGER.info("CHECK SERVER CERTIFICATE: {}", chain[0].getSubjectDN());
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
