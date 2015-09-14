package userAgent;

public class GuessACK {
	private boolean ack;
	
	public GuessACK(){
		ack = false;
	}
	
	public void setACK(){
		ack = true;
	}
	
	/**
	 * Method that checks if the ack if true. If it is it resets it to false.
	 * @return the value of ack
	 */
	public boolean testAndResetACK(){
		if(ack == true){
			ack = false;
			return true;
		}
		return false;
	}
}
