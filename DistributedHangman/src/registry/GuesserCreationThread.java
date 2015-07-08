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

public class GuesserCreationThread extends Thread{
	private Socket 	socket = null;
	private long 	timeout;
	private ConnectionTimeoutThread connectionTimeout;
	
	public GuesserCreationThread(Socket socket, long timeout){
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
				
				System.out.println("New Guesser");
				
				// BODY
				WaitingRoomLock roomWaitLock;
				String 			userName 		= (String) messageFromClient.get(JSONCodes.userName);
				Boolean 		gameStarting 	= false;
				
				Thread.currentThread().setName(userName);
				
				MyRegistry.addAvailableGuesser(userName); //log guesser to receive available rooms' updates
				
				while(!gameStarting){
					messageFromClient = (JSONObject) new JSONParser().parse(in.readLine());
					roomName = (String) messageFromClient.get(JSONCodes.roomName);
					while(MyRegistry.joinRoom( roomName ) == false){
						messageToClient.put(JSONCodes.message, JSONCodes.guesserJoinError);
						out.println(messageToClient);
						messageFromClient = (JSONObject) new JSONParser().parse(in.readLine());
						roomName = (String) messageFromClient.get(JSONCodes.roomName);
					}
					
					roomWaitLock = MyRegistry.getRoomWaitLock(roomName);
					
					synchronized (roomWaitLock) { 
						/* wait for:	- the master to leave
						 *				- the game to start
						 *				- the timeout to expire
						 */
						try {
							
							if(gameStarting = roomWaitLock.checkIfGameStarting()){ // checkIfGameStart notifies users if needed
								password		= roomWaitLock.getPassword();
								multicastAddr 	= roomWaitLock.getMulticast();
							} else {
								roomWaitLock.wait();
								if(gameStarting = roomWaitLock.checkIfGameStarting()){ // check if woke up by master leaving
									password		= roomWaitLock.getPassword();
									multicastAddr 	= roomWaitLock.getMulticast();
								}
							}													
						} catch (InterruptedException e) {
							// timeout expired
							MyRegistry.leaveRoom(roomName);
							MyRegistry.removeAvailableGuesser(userName);
							gameStarting = false;
						}
					}
					
					if(!gameStarting){ // room closed because master or guesser left
						messageToClient.put(JSONCodes.message, JSONCodes.roomClosed);
						out.println(messageToClient); // notify user to get the new room to join
					}else{ // game starting
						messageToClient.put(JSONCodes.message, JSONCodes.gameStarting);
						messageToClient.put(JSONCodes.roomPassword, password);
						messageToClient.put(JSONCodes.roomMulticast, multicastAddr);
						out.println(messageToClient); // send info to start game
					}
				}
				// game is starting
				
				
				// END BODY
				
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
