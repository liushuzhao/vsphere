package cnitsec.common.connection;

import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class MyTrustManager {
	/*
	 * 信任一切证书，不推荐的方法
	 */
	@Deprecated
	public void trustAll() {
		// Declare a host name verifier that will automatically enable
		// the connection. The host name verifier is invoked during
		// the SSL handshake.
		javax.net.ssl.HostnameVerifier hv = new javax.net.ssl.HostnameVerifier() {
		public boolean verify(String urlHostName, javax.net.ssl.SSLSession session) {
		return true;
		}
		};
		 
		// Create the trust manager.
		javax.net.ssl.TrustManager[] trustAllCerts = new javax.net.ssl.TrustManager[1];
		javax.net.ssl.TrustManager tm = new TrustAllTrustManager();
		trustAllCerts[0] = tm;
		 
		// Create the SSL context
		javax.net.ssl.SSLContext sc = null;
		try {
			sc = javax.net.ssl.SSLContext.getInstance("SSL");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}
		 
		// Create the session context
		javax.net.ssl.SSLSessionContext sslsc = sc.getServerSessionContext();
		 
		// Initialize the contexts; the session context takes the trust manager.
		sslsc.setSessionTimeout(0);
		try {
			sc.init(null, trustAllCerts, null);
		} catch (KeyManagementException e) {
			e.printStackTrace();
		}
		 
		// Use the default socket factory to create the socket for the secure connection
		javax.net.ssl.HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());
		 
		// Set the default host name verifier to enable the connection.
		javax.net.ssl.HttpsURLConnection.setDefaultHostnameVerifier(hv);
	}
	
	/*
	 * 指定trusttore，在该truststore中的证书将被信任。推荐的方法。
	 */
	public void trustServer() throws KeyManagementException, NoSuchAlgorithmException{
		Properties systemProps = System.getProperties();  
		systemProps.put( "javax.net.ssl.trustStore", "C:\\VMware-Certs\\vmware.keystore");  
		systemProps.put( "javax.net.ssl.trustStorePassword", "19920717");  
		System.setProperties(systemProps); 
	}
	
	/*
	 * 用于方法trustAll的TrustManager类(内部类)
	 */
	private static class TrustAllTrustManager implements javax.net.ssl.TrustManager,javax.net.ssl.X509TrustManager {
		public java.security.cert.X509Certificate[] getAcceptedIssuers() {
			return null;
		}
	 
		public void checkServerTrusted(java.security.cert.X509Certificate[] certs,String authType)throws java.security.cert.CertificateException {
			return;
		}
	 
		public void checkClientTrusted(java.security.cert.X509Certificate[] certs,String authType)throws java.security.cert.CertificateException {
			return;
		}
	}
}
