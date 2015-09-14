package registry;

public class WaitingRoom {
	private String roomName;
	private int currentGuessers, requiredGuessers;
	private Object 	numberOfGuessersLock	= new Object();
	private WaitingRoomLock roomWaitLock;
	
	public WaitingRoom(String roomName, int requiredPlayers, WaitingRoomLock roomWait){
		this.roomName = roomName;
		this.requiredGuessers = requiredPlayers;
		this.currentGuessers = 0;
		this.roomWaitLock = roomWait;
	}
	
	public String getRoomInfo(){
		return roomName+": "+currentGuessers+"/"+requiredGuessers;
	}
	
	public WaitingRoomLock getWaitLock(){
		return roomWaitLock;
	}
	
	public void removeGuesser(){
		synchronized (numberOfGuessersLock) {
				currentGuessers--;
		}
	}
	
	/**
	 * Method that adds a new guesser to the room. If the guesser was the last one needed it sets the room's state to "gameStarting" 
	 * @return true if the room was not full, false otherwise. 
	 */
	public boolean addGuesser(){
		boolean result;
		
		synchronized (numberOfGuessersLock) {
			
			if(currentGuessers >= requiredGuessers)
				result = false;
			else{
				currentGuessers++;
				result = true;
			}
			if(result && currentGuessers == requiredGuessers)
				roomWaitLock.setGameStarting();
		}
	return result;
	}
}
