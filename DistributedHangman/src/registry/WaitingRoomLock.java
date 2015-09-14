package registry;

import encryption.PasswordGenerator;

public class WaitingRoomLock {
	private boolean gameStarting, usersNotified;
	private String roomPassword;
	private String roomMulticast;
	
	public WaitingRoomLock(){
		gameStarting = false;
		usersNotified = false;
		roomMulticast = MulticastAddrGenerator.getMulticastAddress();
		roomPassword = PasswordGenerator.nextPassword();
	}
	
	
	public void setGameStarting(){
		gameStarting = true;
	}
	
	/**
	 * Method used to check if the game is starting. If it is it notifies all the waiting users.
	 * @return true if the game is starting, false otherwise.
	 */
	public boolean checkIfGameStarting(){
		if(gameStarting && !usersNotified){
			usersNotified = true;
			this.notifyAll();
		}
		return gameStarting;
	}
	public String getPassword(){
		return this.roomPassword;
	}
	public String getMulticast(){
		return this.roomMulticast;
	}
}
