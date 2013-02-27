package server;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Time;
import java.text.SimpleDateFormat;

public class Logger {
	private final static String logFileName = "medicalrecord.log";
	public static String newline = System.getProperty("line.separator");
	private File logFile;
	private SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd - HH:mm:ss: ");
	Time currentTime;
	
	public Logger() {
		logFile = new File(logFileName);
	}

	public void log(String message) {
		FileWriter out;
		try {
			String time = sdf.format(new Time(System.currentTimeMillis()).getTime());
			out = new FileWriter(logFile, true);
			out.write(time + message + newline);
			out.close();
		} catch (IOException e) {
			System.out.println("Could not open file");
		}
	}

	public void logFinish(){
		log("Finished");
	}
	public void logStart(){
		log("Start");
	}
}
