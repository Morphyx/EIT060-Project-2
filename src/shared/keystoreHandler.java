package shared;

import java.io.FileInputStream;
import java.security.KeyStore;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;

public class keystoreHandler {
	private final static String PATH = "Keystores/";
	private static String TRUSTEDPASS = "password";
	private static String TRUSTED = "truststore";
	
    
    public static SSLContext getContext(String keystore, String password){
        SSLContext ctx = null;
		try {
	    	KeyManagerFactory kmf;
	        KeyStore ks;
	        char[] passphrase = password.toCharArray();
			ctx = SSLContext.getInstance("TLS");
	        kmf = KeyManagerFactory.getInstance("SunX509");
	        ks = KeyStore.getInstance("JKS");
	        ks.load(new FileInputStream(PATH+keystore), passphrase);
	        kmf.init(ks, passphrase);
	        ctx.init(kmf.getKeyManagers(), getTrusted(), null);
		} catch (Exception e) {
			e.printStackTrace();
		}
        return ctx;
    }
	
	private static TrustManager[] getTrusted(){
		TrustManager[] trustManagers = null;
		try {
	    	KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
			String trustPassword = TRUSTEDPASS;
			trustStore.load(new FileInputStream(PATH+TRUSTED), trustPassword.toCharArray());
			TrustManagerFactory trustFactory = 
			  TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());    
			trustFactory.init(trustStore);
			trustManagers = trustFactory.getTrustManagers();
		} catch (Exception e) {
			e.printStackTrace();
		}
		return trustManagers; 
	}
}
