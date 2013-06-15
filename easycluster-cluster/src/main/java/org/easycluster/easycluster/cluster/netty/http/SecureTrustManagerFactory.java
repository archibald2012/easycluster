package org.easycluster.easycluster.cluster.netty.http;

import java.security.InvalidAlgorithmParameterException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.cert.X509Certificate;

import javax.net.ssl.ManagerFactoryParameters;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactorySpi;
import javax.net.ssl.X509TrustManager;

public class SecureTrustManagerFactory extends TrustManagerFactorySpi {
  
      private static final TrustManager DUMMY_TRUST_MANAGER = new X509TrustManager() {
          public X509Certificate[] getAcceptedIssuers() {
              return new X509Certificate[0];
          }
  
          public void checkClientTrusted(X509Certificate[] chain, String authType) {
              // Always trust - it is an example.
              // You should do something in the real world.
              // You will reach here only if you enabled client certificate auth,
              // as described in SecureChatSslContextFactory.
              System.err.println(
                      "UNKNOWN CLIENT CERTIFICATE: " + chain[0].getSubjectDN());
          }
  
          public void checkServerTrusted(X509Certificate[] chain, String authType) {
              // Always trust - it is an example.
              // You should do something in the real world.
              System.err.println(
                      "UNKNOWN SERVER CERTIFICATE: " + chain[0].getSubjectDN());
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
      protected void engineInit(ManagerFactoryParameters managerFactoryParameters)
              throws InvalidAlgorithmParameterException {
          // Unused
      }
  }