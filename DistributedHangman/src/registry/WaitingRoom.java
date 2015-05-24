package registry;

import java.util.Vector;

public class WaitingRoom {
	private String roomName, password, multicastAddress;
	private int currentGuessers, requiredGuessers;
	private Vector<String> guessers;
	private Object 	numberOfGuessersLock	= new Object();
	private WaitingRoomLock roomWaitLock;
	
	public WaitingRoom(String roomName, int requiredPlayers, String password, String multicastAddress, WaitingRoomLock roomWait){
		this.roomName = roomName;
		this.requiredGuessers = requiredPlayers;
		this.currentGuessers = 0;
		this.password = password;
		this.multicastAddress = multicastAddress;
		this.roomWaitLock = roomWait;
		guessers = new Vector<String>();
	}
	
	public String getRoomInfo(){
		return roomName+": "+currentGuessers+"/"+requiredGuessers;
	}
	
	public String getPassword(){
		return this.password;
	}
	
	public String getMulticastAddress(){
		return this.multicastAddress;
	}
	
	public WaitingRoomLock getWaitLock(){
		return roomWaitLock;
	}
	
	public boolean removeGuesser(String exGuesser){
		boolean result;
		synchronized (numberOfGuessersLock) {
			if(guessers.remove(exGuesser)){
				currentGuessers--;
				result = true;
			} else {
				result = false;
			}
		}
		return result;
	}
	
	public void removeMaster(){
		synchronized (roomWaitLock) {
			// wake up all waiting threads
			roomWaitLock.notifyAll();
		}
	}
	
	public boolean addGuesser(String userName){
		boolean result;
		
		synchronized (numberOfGuessersLock) {
			
			if(currentGuessers >= requiredGuessers)
				result = false;
			else{
				guessers.addElement(userName);
				currentGuessers++;
				result = true;
			}
			
			if(result && currentGuessers == requiredGuessers){
				synchronized (roomWaitLock) {
					roomWaitLock.setGameStarting();
					// game is ready to start, wake up all waiting threads
					roomWaitLock.notifyAll();
				}
			}
		}
		return result;
	}
}
