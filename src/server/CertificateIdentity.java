package server;

public class CertificateIdentity {
	private String O,OU,CN;
	
	public CertificateIdentity(String O, String OU, String CN){
		this.O = O;
		this.OU = OU;
		this.CN = CN;
	}
	
	public String getOU(){
		return OU;
	}
	
	public boolean hasReadAccess(Journal j){
		if(this.O.equals("Patient")){
			return j.patient_id.equals(this.CN);
		}else if(this.O.equals("Nurse")){
			return j.nurse_id.equals(this.CN) || 
					j.division_id.equals(this.OU);
		}else if(this.O.equals("Doctor")){
			return j.doctor_id.equals(this.CN) ||
					j.division_id.equals(this.OU);
		}else if(this.O.equals("Agency")){
			return true;
		}
		return false;
		
	}
	
	public boolean hasWriteAccess(Journal j){
		if(this.O.equals("Patient")){
			return false;
		}else if(this.O.equals("Nurse")){
			return j.nurse_id.equals(this.CN);
		}else if(this.O.equals("Doctor")){
			return j.doctor_id.equals(this.CN);
		}else if(this.O.equals("Agency")){
			return false;
		}
		return false;
		
	}
	
	public boolean hasCreateAccess(Journal j){
		if(this.O.equals("Doctor")){
			return j.doctor_id.equals(this.CN);
		}
		return false;
		
	}
	
	public boolean hasDeleteAccess(Journal j){
		if(this.O.equals("Agency")){
			return true;
		}
		return false;
		
	}
}
