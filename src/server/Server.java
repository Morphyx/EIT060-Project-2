package server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Scanner;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLPeerUnverifiedException;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSession;
import javax.net.ssl.SSLSocket;
import javax.security.cert.X509Certificate;
import java.util.Properties;

import com.google.gson.Gson;

import shared.keystoreHandler;


public class Server extends Thread{
	public static final int INDEX = 0;
	public static final int CREATE = 1;
	public static final int SHOW = 2;
	public static final int UPDATE = 3;
	public static final int DESTROY = 4;	
	public static final int LOGOUT = 5;	
	
	private static final byte[] CRLF = { 13, 10 };
	private ArrayList<Journal> journalList;
	private final String JOURNALFILE = "journalfile.txt";
	private int current_journal_id;
	private CertificateIdentity currentConnectedUser;
	private boolean firstRun = true;
	
	
	public static void main(String[]args) throws IOException{		
		Server server = new Server(9999, "admin", "adminpass");
		server.waitForConnection();
	}

	private InputStream is;
	private OutputStream os;
	private int port;
	private String keystore, password;
	private Properties properties;
	private  SSLSocket socket;
	private SSLServerSocket serverSocket;
	public Server(int port, String keystore, String password) throws IOException{
		this.port = port;
		this.keystore = keystore;
		this.password = password;
		journalList = new ArrayList<Journal>();
		
		properties = new Properties();
		try{
			properties.load(new FileInputStream("config.properties"));			
			current_journal_id = Integer.parseInt(properties.getProperty("current_journal_id"));
		}catch (IOException e){
			current_journal_id = 0;
		}
		
		readJournalListFromFile();
//		System.out.println(journalList.size());
//		
//		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
//		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
//		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
//		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
//		
//		saveJournalListToFile();
	}
	
	public int get_journal_id(){
		current_journal_id++;
		properties.setProperty("current_journal_id", Integer.toString(current_journal_id));
		try {
			properties.store(new FileOutputStream("config.properties"), null);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return current_journal_id-1;
	}
	
    private void readJournalListFromFile() throws IOException {
    	File file = new File(JOURNALFILE);
    	if(file.exists()) {
   		 	Gson gson = new Gson();
    		Scanner scan = new Scanner(file);
    		while(scan.hasNextLine()) {
    			journalList.add(gson.fromJson(scan.nextLine(), Journal.class));
    		}
    	} else {
    		file.createNewFile();
    	}
	}
    
    private void saveJournalListToFile() throws IOException{
    	File file = new File(JOURNALFILE);
		Gson gson = new Gson();

    	if(!file.exists()) {
    		file.createNewFile();
    	}
    	FileWriter fw = new FileWriter(file, false);
    	for(Journal j :journalList){
    		fw.write(gson.toJson(j) + "\n");
    	}
    	fw.close();
    }

	public void waitForConnection() {
        try {
            SSLContext context = keystoreHandler.getContext(keystore, password);
            SSLServerSocketFactory factory = context.getServerSocketFactory();
            serverSocket =
                    (SSLServerSocket) factory.createServerSocket(port);
            serverSocket.setNeedClientAuth(true);
            System.out.println("Waiting for client to connect...");
            socket = (SSLSocket) serverSocket.accept();
            System.out.println("Authentication successfull!");
            
            is = socket.getInputStream();
            os = socket.getOutputStream();
            registerSession(socket);
            if(firstRun){
            	this.start();
            	firstRun = false;
            }
        } catch (Exception exception) {
        	System.out.println("port already in use");
        	System.exit(0);
            exception.printStackTrace();
        }
    }
    
	private void closeConnection() {
		try {
			is.close();
			os.close();
			serverSocket.close();
			socket.close();
		} catch (IOException e) {
		}
	}
    
    public void registerSession(SSLSocket socket) throws SSLPeerUnverifiedException{
    	SSLSession session = socket.getSession();
        X509Certificate cert = (X509Certificate)session.getPeerCertificateChain()[0];
        String subject = cert.getSubjectDN().getName();
        String[] strings = subject.split(", ");
        
        currentConnectedUser = new CertificateIdentity(strings[2].split("=")[1],strings[1].split("=")[1], 
        		strings[0].split("=")[1]);
        
        System.out.println ("Communicatng with: " + subject);
        
        
        
        //spara undan subject (tänk på loggföring)
        // vid disconect sätt currentConnectedUser till null
        
    }
    
    public void run(){
    	System.out.println("Server up and running!!");
    	while(true){
    		try {
    			int index;
    			Gson gson = new Gson();
    			int command = Integer.parseInt(getLine(is));
    			Journal journal = null;
    			if (command != INDEX){
    				journal = gson.fromJson(getLine(is), Journal.class);
    			}
    			switch(command){
    				case (INDEX):
    					ArrayList<Journal> accessedJournals = new ArrayList<Journal>();
    					for(Journal j: journalList){
    						if(currentConnectedUser.hasReadAccess(j))
    							accessedJournals.add(j);
    					}
    					putLine(os, gson.toJson(accessedJournals));
    				break;
    				case (CREATE):
    					
    					if(currentConnectedUser.hasCreateAccess(journal)){
    						journal.division_id = currentConnectedUser.getOU();
    						journal.id = get_journal_id();
							saveJournalListToFile();
    						journalList.add(journal);
							saveJournalListToFile();
							putLine(os, "Successfully created journal with id: " + journal.id);
    					}else{
							putLine(os, "no access to create that journal");
    					}
    				break;
    				case (SHOW):
    					index = journalList.indexOf(journal);
    					if (index == -1){
							putLine(os, "no journal with id: " + journal.id);
    					}else{
    						Journal foundJournal = journalList.get(journalList.indexOf(journal));
    						
    						if(currentConnectedUser.hasReadAccess(foundJournal)){
    							putLine(os, gson.toJson(foundJournal));							
    						}else{
    							putLine(os, "no showaccess for journal with id: " + journal.id);
    						}
    					}
    				break;
    				case (UPDATE):
    					index = journalList.indexOf(journal);
					if (index == -1){
						putLine(os, "no journal with id: " + journal.id);
					}else{
						Journal foundJournal = journalList.get(journalList.indexOf(journal));
						//TODO testa om objektet blir uppdaterat!
						if(currentConnectedUser.hasWriteAccess(foundJournal)){
							foundJournal.journaltext = journal.journaltext;
							saveJournalListToFile();
							putLine(os, "suucessfully updated journal");							
						}else{
							putLine(os, "no showaccess for journal with id: " + journal.id);
						}
					}
    				break;
    				case (DESTROY):
    					index = journalList.indexOf(journal);
						if (index == -1){
							putLine(os, "no journal with id: " + journal.id);
						}else{
							//TODO visa ej om inte journal finns, access denied först och främst
							Journal foundJournal = journalList.get(journalList.indexOf(journal));
							
							if(currentConnectedUser.hasDeleteAccess(foundJournal)){
								putLine(os, gson.toJson(foundJournal));							
							}else{
								putLine(os, "no deleteaccess for journal with id: " + journal.id);
							}
						}
    				break;
    				case (LOGOUT):
    				closeConnection();
    				System.out.println("User logged out.");
    				waitForConnection();
    				break;
    				default:
    					System.out.println("Kommando: " + command + " finns ej");
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
	private static String getLine(InputStream s) throws IOException {
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
