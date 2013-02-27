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
	private ArrayList<Journal> journalList;
	private final String JOURNALFILE = "journalfile.txt";
	private int current_journal_id;
	
	
	public static void main(String[]args) throws IOException{		
		Server server = new Server(9999, "admin", "adminpass");
		server.waitForConnection();
	}

	private InputStream is;
	private OutputStream os;
	private int port;
	private String keystore, password;
	private Properties properties;
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
		System.out.println(journalList.size());
		
		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
		journalList.add(new Journal(this, "asd", "asdfsd", "asdf", "sdf", "asdf"));
		
		saveJournalListToFile();
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
    	System.out.println(journalList.size());
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
        	System.out.println("port already in use");
        	System.exit(0);
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
