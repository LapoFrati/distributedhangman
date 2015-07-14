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
