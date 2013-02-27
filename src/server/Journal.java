package server;

import com.google.gson.Gson;

public class Journal implements Comparable<Journal>{
	int id = -1;
	String created_doctor_id;
	String doctor_id;
	String nurse_id;
	String patient_id;
	String journaltext;
	
	public Journal(Server server, String created_docotr_id, String doctor_id, String nurse_id, String patient_id, String journaltext){
		this.created_doctor_id = created_docotr_id;
		this.doctor_id = doctor_id;
		this.nurse_id = nurse_id;
		this.patient_id = patient_id;
		this.journaltext = journaltext;
		this.id = server.get_journal_id();
	}
	
	
	
	
	
//	public static void main(String args[]){
//		 Gson gson = new Gson();
//		 Journal target = new Journal("A", "B", "A", "C", "blablabla");
//		 String json = gson.toJson(target);
//		 System.out.println(json);
//		 
//		 Journal target2 = gson.fromJson(json, Journal.class);
//		 System.out.println(target2.id);
//	}
//


	@Override
	public int compareTo(Journal arg0) {
		return this.id - arg0.id;
	}
}
