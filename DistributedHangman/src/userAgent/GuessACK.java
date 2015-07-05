package userAgent;

public class GuessACK {
	private boolean ack;
	
	public GuessACK(){
		ack = false;
	}
	
	public void setACK(){
		ack = true;
	}
	
	public boolean testAndResetACK(){
		if(ack == true){
			ack = false;
			return true;
		}
		
		return false;
	}
}
