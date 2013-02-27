package client;

import java.io.*;
import javax.net.ssl.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import shared.keystoreHandler;

public class Client extends Thread{
	public static void main(String[]args){
		Client client = new Client("localhost", 9999, "nurseA", "nurseA");
		client.connect();
	}

	private InputStream is;
	private OutputStream os;
	private String hostname, keystore, password;
	private int port;
	
	public Client(String hostname, int port, String keystore, String password){
		this.hostname = hostname;
		this.port = port;
		this.keystore = keystore;
		this.password = password;
	}
    public void connect() {
            try {           	
                SSLContext context = keystoreHandler.getContext(keystore, password);
            	SSLSocketFactory factory = context.getSocketFactory();
                SSLSocket socket = (SSLSocket)factory.createSocket(hostname, port);
                socket.setUseClientMode(true);
                socket.startHandshake();
                System.out.println("Authentication successfull!");
                
                is = socket.getInputStream();
                os = socket.getOutputStream();
                registerSession(socket);
                this.start();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
    }
    
    public void registerSession(SSLSocket socket) throws SSLPeerUnverifiedException{
    	SSLSession session = socket.getSession();
        X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
        String subject = cert.getSubjectDN().getName();
        System.out.println ("Communicatng with: " + subject);
        //spara undan subject (tänk på loggföring)
    }
    
    public void run(){
    	System.out.println("Client up and running!!");
    	while(true){
    		//gör requests
    	}
    }
}
