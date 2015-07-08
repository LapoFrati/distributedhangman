package registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import messages.JSONCodes;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterCreationThread extends Thread{
	private Socket 	socket = null;
	private long 	timeout;
	private ConnectionTimeoutThread connectionTimeout;
	
	public MasterCreationThread(Socket socket, long timeout){
		this.socket = socket;
		this.timeout = timeout;
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		// start timeout to close the socket
		connectionTimeout = new ConnectionTimeoutThread(socket, timeout, Thread.currentThread());
		connectionTimeout.start();
		try (
	            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	            BufferedReader in = new BufferedReader(
	                new InputStreamReader(
	                    socket.getInputStream()));
	        ) {

	        try {
				JSONObject 	messageFromClient 	= (JSONObject) new JSONParser().parse(in.readLine()),
							messageToClient 	= new JSONObject();
				String 	roomName		= "",
						password 		= "",
						multicastAddr 	= "";
				
				System.out.println("New Master");
				
				WaitingRoomLock roomWaitLock;
				WaitingRoom waitingRoom; 
				int 	requiredGuessers 	= ((Long) messageFromClient.get(JSONCodes.numberOfGuessers)).intValue();
				boolean masterLeft	 		= false;
						
				roomName = (String) messageFromClient.get(JSONCodes.roomName);
				
				Thread.currentThread().setName(roomName);
				
				
				// Since the fixedPool's size for the masters is 10, there can't be more than 10 master active simultaneously -> no more than 10 rooms
				waitingRoom = MyRegistry.createNewWaitingRoom(roomName, requiredGuessers);
				
				roomWaitLock = waitingRoom.getWaitLock();
				
				if(!masterLeft)
					messageToClient.put(JSONCodes.message, JSONCodes.newRoomCreated);
				else
					messageToClient.put(JSONCodes.message, JSONCodes.roomClosed);
					
				out.println(messageToClient);
				
				if(!masterLeft){
					synchronized (roomWaitLock) {	
						try {
							roomWaitLock.wait();
							// if master wakes up as planned the game is starting
							password		= roomWaitLock.getPassword();
							multicastAddr 	= roomWaitLock.getMulticast();
						} catch (InterruptedException e) {
							//if master gets interrupted the timeout has finished
							roomWaitLock.notifyAll(); // wake up all guessers
							masterLeft = true;
						}
						
						MyRegistry.closeWaitingRoom(roomName, waitingRoom); // close room and stop new guessers
					}
				}
				
				// Prepare message to client
				if(!masterLeft){ // send information needed to start the game
					messageToClient.put(JSONCodes.message, JSONCodes.gameStarting);
					messageToClient.put(JSONCodes.roomPassword, password);
					messageToClient.put(JSONCodes.roomMulticast, multicastAddr);
				} else { // acknowledge the master leaving the room
					// TODO avoid this message since master requested it?
					messageToClient.put(JSONCodes.message, JSONCodes.roomClosed);
				}
				out.println(messageToClient);
				
				
	        } catch (ParseException e) {
				e.printStackTrace();
			}
	        
	        	closeConnection();
	        	
	        	System.out.println("joined Timeout Thread");
	        	
	        } catch (IOException e) {
	            e.printStackTrace();
	        }
	}
	
	private void closeConnection(){
		try { // terminate connectionTimeout's alarm to close the socket.
    		connectionTimeout.disableInterrupt = true;
    		connectionTimeout.interrupt();
			connectionTimeout.join();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}