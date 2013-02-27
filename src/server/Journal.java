package server;

import com.google.gson.Gson;

public class Journal implements Comparable<Journal>{
	int id = -1;
	String doctor_id;
	String nurse_id;
	String patient_id;
	String journaltext;
	String division_id;
	
	public Journal(int id, String doctor_id, String nurse_id, String patient_id, String journaltext, String division_id){
		this.doctor_id = doctor_id;
		this.nurse_id = nurse_id;
		this.patient_id = patient_id;
		this.journaltext = journaltext;
		this.id = id;
		this.division_id = division_id;
	}
	public Journal(String doctor_id, String nurse_id, String patient_id, String journaltext, String division_id){
		this.doctor_id = doctor_id;
		this.nurse_id = nurse_id;
		this.patient_id = patient_id;
		this.journaltext = journaltext;
		this.division_id = division_id;
	}
	public Journal(String doctor_id, String nurse_id, String patient_id, String journaltext){
		this.doctor_id = doctor_id;
		this.nurse_id = nurse_id;
		this.patient_id = patient_id;
		this.journaltext = journaltext;
	}
	
	public Journal(int id){
		this.id = id;
	}
	
	public Journal(int id, String journaltext){
		this.id = id;
		this.journaltext = journaltext;
	}

	public String toString(){
		return "id: " + this.id + " Doctor: " + this.doctor_id + " Nurse: " + this.nurse_id + 
				" Patient: " + this.patient_id + " Division: " + this.division_id + " Information: " + this.journaltext;
	}
	
	@Override
	public int compareTo(Journal arg0) {
		return this.id - arg0.id;
	}
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + id;
		return result;
	}
	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Journal other = (Journal) obj;
		if (id != other.id)
			return false;
		return true;
	}

}
