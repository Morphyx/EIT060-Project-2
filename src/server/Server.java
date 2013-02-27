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
	private int current_journal_id = 0;
	private CertificateIdentity currentConnectedUser;
	private boolean firstRun = true;
	private Logger logger;
	private Gson gson = new Gson();
	
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
		logger = new Logger();
		logger.logStart();
		
		this.port = port;
		this.keystore = keystore;
		this.password = password;
		journalList = new ArrayList<Journal>();
		
		properties = new Properties();
		try{
			properties.load(new FileInputStream("config.properties"));			
			current_journal_id = Integer.parseInt(properties.getProperty("current_journal_id"));
		}catch (IOException e){	}
		
		readJournalListFromFile();
	}

    private void readJournalListFromFile() {
    	logger.log("reading journallist from file");
    	File file = new File(JOURNALFILE);
    	if(file.exists()) {
    		Scanner scan;
			try { 
				scan = new Scanner(file);
				while(scan.hasNextLine()) {
					journalList.add(gson.fromJson(scan.nextLine(), Journal.class));
				}
			} catch (FileNotFoundException e) {}
    	} else {
        	logger.log("journallistfile did not exist, creating a new one...");
    		try {
				file.createNewFile();
			} catch (IOException e) {
	        	logger.log("IOException when creating new journallistfile");
			}
    	}
	}
    
	public int get_journal_id(){
		logger.log("getting new journalid: " + current_journal_id++);
		
		properties.setProperty("current_journal_id", Integer.toString(current_journal_id));
		try {
			logger.log("saving new journalid to properties file...");
			properties.store(new FileOutputStream("config.properties"), null);
		} catch (FileNotFoundException e) {
			logger.log("FileNotFoundException when saving new journalid to properties file");
		} catch (IOException e) {
			logger.log("IOException when saving new journalid to properties file");
		}
		return current_journal_id-1;
	}
    
    private void saveJournalListToFile(){
		logger.log("saving journallist to file...");
    	File file = new File(JOURNALFILE);
    	FileWriter fw;
		try {
			fw = new FileWriter(file, false);
			for(Journal j :journalList){
				fw.write(gson.toJson(j) + "\n");
			}
			fw.close();
		} catch (IOException e) {
			logger.log("IOException when saving journallist to file");
		}
    }

	public void waitForConnection() {
		logger.log("waiting for client to connect...");
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
    		logger.log("port already in use, exiting...");
        	System.out.println("port already in use");
        	System.exit(0);
            exception.printStackTrace();
        }
    }
    
	private void closeConnection() {
		logger.log("closing connection");
		try {
			currentConnectedUser = null;
			is.close();
			os.close();
			serverSocket.close();
			socket.close();
		} catch (IOException e) {
		}
	}
    
    public void registerSession(SSLSocket socket){
    	SSLSession session = socket.getSession();
        X509Certificate cert;
        
		try {
			cert = (X509Certificate)session.getPeerCertificateChain()[0];
			String subject = cert.getSubjectDN().getName();
			String[] strings = subject.split(", ");
			currentConnectedUser = new CertificateIdentity(strings[2].split("=")[1],strings[1].split("=")[1], 
					strings[0].split("=")[1]);
			logger.log("client connected: " + subject);
		} catch (SSLPeerUnverifiedException e) {
			logger.log("SSLPeerUnverifiedException when connecting client");
			e.printStackTrace();
		}
    }
    
    public void run(){
    	System.out.println("Server up and running!");
    	while(true){
    		try {
    			int journalindex;
    			String commandString = getLine(is);
    			logger.log("recieved request from client: " + commandString);
    			int command = Integer.parseInt(commandString);
    			
    			Journal journal = null;
    			if (command != INDEX){
    				String journalString = getLine(is);
    				journal = gson.fromJson(journalString, Journal.class);
        			logger.log("recieved journal from client: " + journalString);
    			}

    			switch(command){
    				case (INDEX):
    					ArrayList<Journal> accessedJournals = new ArrayList<Journal>();
    					for(Journal j: journalList){
    						if(currentConnectedUser.hasReadAccess(j))
    							accessedJournals.add(j);
    					}
            			logger.log("recieved INDEX request, returning " + accessedJournals.size() + " journals available to the user");
    					putLine(os, gson.toJson(accessedJournals));
    				break;
    				case (CREATE):
    					if(currentConnectedUser.hasCreateAccess(journal)){
    						journal.division_id = currentConnectedUser.getOU();
    						journal.id = get_journal_id();
    						journalList.add(journal);
    						logger.log("recieved CREATE request, create succeeded with the following journal: " + gson.toJson(journal));
							saveJournalListToFile();
							putLine(os, "Successfully created journal with id: " + journal.id);
    					}else{
	            			logger.log("recieved CREATE request, failed accesscontrol for the following journal: " + gson.toJson(journal));
							putLine(os, "no access to create that journal");
    					}
    				break;
    				case (SHOW):
    					journalindex = journalList.indexOf(journal);
    					if (journalindex == -1){
                			logger.log("recieved SHOW request, no journal found with id: " + journal.id);
							putLine(os, "no showaccess for journal with id: " + journal.id);
    					}else{
    						Journal foundJournal = journalList.get(journalList.indexOf(journal));
    						
    						if(currentConnectedUser.hasReadAccess(foundJournal)){
                    			logger.log("recieved SHOW request, show succeeded for journal with id: " + journal.id);
    							putLine(os, gson.toJson(foundJournal));							
    						}else{
                    			logger.log("recieved SHOW request, access failed for journal with id: " + journal.id);
    							putLine(os, "no showaccess for journal with id: " + journal.id);
    						}
    					}
    				break;
    				case (UPDATE):
    					journalindex = journalList.indexOf(journal);
						if (journalindex == -1){
							putLine(os, "no updateaccess for journal with id: " + journal.id);
                			logger.log("recieved UPDATE request, no journal found with id: " + journal.id);
						}else{
							Journal foundJournal = journalList.get(journalList.indexOf(journal));
							if(currentConnectedUser.hasWriteAccess(foundJournal)){
								foundJournal.journaltext = journal.journaltext;
								logger.log("recieved UPDATE request, update succeeded for journal: " + gson.toJson(journal));
								saveJournalListToFile();
								putLine(os, "sucessfully updated journal");							
							}else{
	                			logger.log("recieved UPDATE request, access failed for journal with id: " + journal.id);
								putLine(os, "no updateaccess for journal with id: " + journal.id);
							}
						}
    				break;
    				case (DESTROY):
    					journalindex = journalList.indexOf(journal);
						if (journalindex == -1){
                			logger.log("recieved DESTROY request, no journal found with id: " + journal.id);
                			if(currentConnectedUser.getO().equals("Agency")){//tillåt administratör att veta om journalen existerar
                				putLine(os, "no journal with id: " + journal.id);
                			}else{
								putLine(os, "no deleteaccess for journal with id: " + journal.id);
                			}
						}else{
							Journal foundJournal = journalList.get(journalList.indexOf(journal));
							
							if(currentConnectedUser.hasDeleteAccess(foundJournal)){
								journalList.remove(foundJournal);
								logger.log("recieved DESTROY request, delete succeeded for journal with id: " + journal.id);
								saveJournalListToFile();
								putLine(os, gson.toJson(foundJournal));							
							}else{
	                			logger.log("recieved DESTROY request, access failed for journal with id: " + journal.id);
								putLine(os, "no deleteaccess for journal with id: " + journal.id);
							}
						}
    				break;
    				case (LOGOUT):
            			logger.log("user logged off");
    					System.out.println("User logged out.");
	    				closeConnection();
	    				waitForConnection();
    				break;
    				default:
            			logger.log("recieved unknown request: " + command);
    					System.out.println("Kommando: " + command + " finns ej");
    			}
			} catch (IOException e) {
    			logger.log("IOException when receiving or sending request.");
				e.printStackTrace();
			}
    	}
    }

	private void putLine(OutputStream s, String str) throws IOException {
		logger.log("Send to client: \"" + str + "\"");
		s.write(str.getBytes());
		s.write(CRLF);
	}
	
	private static String getLine(InputStream s) throws IOException {
		boolean done = false;
		String result = "";
		while(!done) {
			int ch = s.read();
			if (ch <= 0 || ch == 10) {
				done = true;
			} else if (ch >= ' ') {
				result += (char)ch;
			}
		}
		return result;
	}
}
