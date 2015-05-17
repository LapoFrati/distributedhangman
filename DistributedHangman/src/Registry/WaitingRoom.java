package Registry;

import java.net.Socket;
import java.util.Vector;

public class WaitingRoom {
	private String roomName, password;
	private int currentPlayers, requiredPlayers;
	private Vector<Socket> playersSockets = null;
	
	public WaitingRoom(String roomName, int requiredPlayers, String password, Socket creatorSocket ){
		this.roomName = roomName;
		this.requiredPlayers = requiredPlayers;
		this.password = password;
		playersSockets.add(creatorSocket);
	}
	
	public String getRoomInfo(){
		return roomName+": "+currentPlayers+"/"+requiredPlayers;
	}
}
