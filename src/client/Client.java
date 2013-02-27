package client;

import java.io.*;

import javax.net.ssl.*;
import javax.security.cert.X509Certificate;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.security.KeyStore;
import java.util.ArrayList;

import server.Journal;
import server.Server;
import shared.keystoreHandler;

public class Client extends Thread{
	public static void main(String[]args) throws IOException{
		Client client = new Client("localhost", 9999);
		client.connect();
	}

	private static final byte[] CRLF = { 13, 10 };
	private String curerntUser = "doctorA";
	
	private InputStream is;
	private OutputStream os;
	private String hostname, keystore, password;
	private int port;
	private SSLSocket socket;
	private boolean firstRun = true;
	
	public Client(String hostname, int port){
		this.hostname = hostname;
		this.port = port;
	}
    public void connect() {
            try {  
            	boolean inCorrect = false;
            	do{
            	inCorrect = false;
        		System.out.print("Type in username: ");
        		keystore = Client.getLine(System.in);
        		System.out.print("Type in password: ");
        		password = Client.getLine(System.in);  	
        		try{
        		SSLContext context = keystoreHandler.getContext(keystore, password);
            	SSLSocketFactory factory = context.getSocketFactory();
                socket = (SSLSocket)factory.createSocket(hostname, port);
        		}catch(IOException e){
        			System.out.println("Wrong username/password combination");
        			inCorrect = true;
        		}
            	}while(inCorrect);
                socket.setUseClientMode(true);
                socket.startHandshake();
//                System.out.println(socket);
//                System.out.println("Authentication successfull!");
                
                is = socket.getInputStream();
                os = socket.getOutputStream();
                registerSession(socket);
                if(firstRun){
                	this.start();
                	firstRun = false;
                }
            } catch (Exception exception) {
                exception.printStackTrace();
            }
    }
    
    public void registerSession(SSLSocket socket) throws SSLPeerUnverifiedException{
    	SSLSession session = socket.getSession();
        X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
        String subject = cert.getSubjectDN().getName();
//        System.out.println ("Communicatng with: " + subject);
        //spara undan subject (tänk på loggföring)
    }
    
    public void run(){
    	System.out.println("Index = " + Server.INDEX + ", Create: " + Server.CREATE + ", Show: " + 
    	Server.SHOW + ", Update: " + Server.UPDATE + ", Delete: " + Server.DESTROY + ", Logout: " + Server.LOGOUT);
    	while(!interrupted()){
    		Gson gson = new Gson();
    		System.out.print("Make request: ");
    		int commando = -1;
    		int journalId = -1;
    		String journaltext = "";
			try {
				commando = Integer.parseInt(getLine(System.in));
			} catch (NumberFormatException e1) {
				commando = -2;
//				e1.printStackTrace();
			} catch (IOException e1) {
				e1.printStackTrace();
			}
    		
    		try {
    			switch(commando){
    			case Server.INDEX:
    				putLine(os, Integer.toString(Server.INDEX));
    				String indexString = getLine(is);
        			ArrayList<Journal> journals = gson.fromJson(indexString, new TypeToken<ArrayList<Journal>>(){}.getType());
        			for(Journal j: journals){
        				System.out.println(j);
        			}
        			
    				break;
    			case Server.CREATE:
    				//inputta text
    				putLine(os, Integer.toString(Server.CREATE));
    				System.out.print("Please input the nurse name: ");
    				String nurse = getLine(System.in);
    				System.out.print("Please input the patient name: ");
    				String patient = getLine(System.in);
    				System.out.print("Please input the journaltext: ");
    				journaltext = getLine(System.in);
    				
					putLine(os, gson.toJson(new Journal(curerntUser, nurse, patient, journaltext), Journal.class));

    				System.out.println(getLine(is));
    				
    				break;
    			case Server.SHOW:
    				putLine(os, Integer.toString(Server.SHOW));
    				
    				System.out.print("Please input the journal id: ");
    				try{
    					journalId = Integer.parseInt(getLine(System.in));
    				}catch(NumberFormatException e){
    				}
    				
					putLine(os, gson.toJson(new Journal(journalId), Journal.class));
    				
    				String line = getLine(is);
    				try{
        				System.out.println(gson.fromJson(line, Journal.class));
    				}catch(Exception e){
    					System.out.println(line);
    				}
    				break;
    			case Server.UPDATE:
      				System.out.print("Please input the journal id: ");
    				try{
    					journalId = Integer.parseInt(getLine(System.in));
    				}catch(NumberFormatException e){
    				}
    				
    				System.out.print("Please input the journaltext: ");
    				journaltext = getLine(System.in);

    				putLine(os, Integer.toString(Server.UPDATE));
					putLine(os, gson.toJson(new Journal(journalId, journaltext), Journal.class));
					
    				System.out.println(getLine(is));
    				break;
    			case Server.DESTROY:
      				System.out.print("Please input the journal id: ");
    				try{
    					journalId = Integer.parseInt(getLine(System.in));
    				}catch(NumberFormatException e){
    				}
    				
    				putLine(os, Integer.toString(Server.DESTROY));
    				putLine(os, gson.toJson(new Journal(journalId), Journal.class));

    				System.out.println(getLine(is));
    				
    				break;
    			case Server.LOGOUT://Logout
    				System.out.println("exiting...");
    				putLine(os, Integer.toString(Server.LOGOUT));
    				is.close();
    				os.close();
    				socket.close();
    				this.connect();
    				break;
    			default:
    				System.out.println("commando unknown");
    			}
    			
    			
    			

    			
    		} catch (IOException e) {
    			e.printStackTrace();
    		}
    	}
    }
    
    
    
    
	private static void putLine(OutputStream s, String str) throws IOException {
		s.write(str.getBytes());
		s.write(CRLF);
	}
	public static String getLine(InputStream s) throws IOException {
		boolean done = false;
		String result = "";
		while(!done) {
			// Read
			int ch = s.read();
			// Something < 0 means end of data (closed socket)
			// ASCII 10 (line feed) means end of line
			if (ch <= 0 || ch == 10) {
				done = true;
			}
			else if (ch >= ' ') {
				result += (char)ch;
			}
		}
		return result;
	}
}
