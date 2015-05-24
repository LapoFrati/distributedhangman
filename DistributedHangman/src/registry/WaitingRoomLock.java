package registry;

public class WaitingRoomLock {
	private boolean gameStarting;
	
	public WaitingRoomLock(){
		gameStarting = false;
	}
	public void setGameStarting(){
		gameStarting = true;
	}
	public boolean checkIfGameStarting(){
		return gameStarting;
	}
}
