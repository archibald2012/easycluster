package org.easycluster.easycluster.cluster.ssl;

import java.io.File;
import java.io.FileInputStream;
import java.security.KeyStore;
import java.security.Security;

import javax.net.ssl.KeyManager;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class SSLContextFactory {

	public SSLContext createSslContext(SSLConfig sslConfig) {

		SSLContext context = null;
		try {
			// Key store (Server side certificate)
			String algorithm = Security.getProperty("ssl.KeyManagerFactory.algorithm");
			if (algorithm == null) {
				algorithm = "SunX509";
			}

			KeyManager[] keyManagers = null;
			if (sslConfig.getKeyStore() != null) {
				String keyStoreLocation = sslConfig.getKeyStore();
				String keyStorePassword = sslConfig.getKeyStorePassword();
				KeyStore ks = KeyStore.getInstance("JKS");
				ks.load(new FileInputStream(new File(keyStoreLocation)), keyStorePassword.toCharArray());
				KeyManagerFactory kmf = KeyManagerFactory.getInstance(algorithm);
				kmf.init(ks, keyStorePassword.toCharArray());
				keyManagers = kmf.getKeyManagers();
			}

			// if it's two-way certification
			TrustManager[] trustManagers = null;
			if (sslConfig.getTrustStore() != null) {
				KeyStore trustStore = KeyStore.getInstance("JKS");
				trustStore.load(new FileInputStream(new File(sslConfig.getTrustStore())), sslConfig.getTrustStorePassword().toCharArray());
				TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
				tmf.init(trustStore);
				trustManagers = tmf.getTrustManagers();
			}
			trustManagers = (trustManagers == null) ? new TrustManager[] { new EasyX509TrustManager(null) } : trustManagers;

			// Initialise the SSLContext to work with our key managers.
			context = SSLContext.getInstance(sslConfig.getProtocol());
			context.init(keyManagers, trustManagers, null);
		} catch (Exception ex) {
			throw new Error("Failed to initialize the server-side SSLContext. " + ex.getMessage(), ex);
		}
		return context;
	}
}

