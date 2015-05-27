package registry;

public class WaitingRoomLock {
	private boolean gameStarting, usersNotified;
	private String roomPassword;
	private String roomMulticast;
	
	public WaitingRoomLock(){
		gameStarting = false;
		usersNotified = false;
	}
	public void setGameStarting(){
		gameStarting = true;
	}
	public boolean checkIfGameStarting(){
		if(gameStarting && !usersNotified){
			usersNotified = true;
			this.notifyAll();
		}
		return gameStarting;
	}
	public void setPassword(String pass){
		this.roomPassword = pass;
	}
	public String getPassword(){
		return this.roomPassword;
	}
	public void setMulticast(String multicast){
		this.roomMulticast = multicast;
	}
	public String getMulticast(){
		return this.roomMulticast;
	}
}
