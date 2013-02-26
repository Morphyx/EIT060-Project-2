package server;

import javax.net.ssl.*;

import java.io.*;
import javax.security.cert.X509Certificate;
import java.security.KeyStore;
import shared.keystoreHandler;


public class Server extends Thread{
	public static void main(String[]args){
		Server server = new Server(9999, "admin", "adminpass");
		server.waitForConnection();
	}

	private InputStream is;
	private OutputStream os;
	private int port;
	private String keystore, password;
	
	public Server(int port, String keystore, String password){
		this.port = port;
		this.keystore = keystore;
		this.password = password;
	}
	
    public void waitForConnection() {
        try {
            SSLContext context = keystoreHandler.getContext(keystore, password);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            SSLServerSocket serverSocket =
                    (SSLServerSocket) factory.createServerSocket(port);
            serverSocket.setNeedClientAuth(true);
            SSLSocket socket = (SSLSocket) serverSocket.accept();
            System.out.println("Authentication successfull!");
            
            is = socket.getInputStream();
            os = socket.getOutputStream();
            registerSession(socket);
            this.run();
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
    	System.out.println("Server up and running!!");
    	while(true){
    		//ta emot requests
    	}
    }

}
