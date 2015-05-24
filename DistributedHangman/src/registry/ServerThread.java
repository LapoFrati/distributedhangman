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

public class ServerThread extends Thread{
	private Socket 	socket = null;
	private long 	timeout;

	
	public ServerThread( Socket socket, long timeout){
		super("ServerThread");
		this.socket = socket;
		this.timeout = timeout;
	}
	
	@SuppressWarnings("unchecked")
	public void run() {
		String role;
		// start timeout to close the socket
		ConnectionTimeoutThread connectionTimeout = new ConnectionTimeoutThread(socket, timeout);
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
						role 				= (String) messageFromClient.get(JSONCodes.role);
			Boolean 	waitCompletion		= true;
			
			System.out.println("New "+role);
			
			switch(role){
				case JSONCodes.master :{
										WaitingRoomLock roomWaitLock = new WaitingRoomLock();
										String 	roomName 			= (String) messageFromClient.get(JSONCodes.roomName);
										int 	requiredGuessers 	= ((Long) messageFromClient.get(JSONCodes.numberOfGuessers)).intValue(),
												waitMultiplier 		= 1;
										boolean gameStarting = false;
										System.out.println("Creating master");
										while(!gameStarting){
											while(MyRegistry.createNewWaitingRoom(roomName, requiredGuessers, roomWaitLock) == false){
												out.println(JSONCodes.waitingRoomsFull);
												
												try { // incremental wait if there are no available rooms
													Thread.sleep(1000*waitMultiplier); 
													waitMultiplier++;
												} catch (InterruptedException e){}
											}
											// here the new room has been created
											messageToClient.put(JSONCodes.message, JSONCodes.newRoomCreated);
											out.println(messageToClient);
											synchronized (roomWaitLock) {
												while(waitCompletion){
													try {
														roomWaitLock.wait();
														waitCompletion = false;
													} catch (InterruptedException e) {
														e.printStackTrace();
													}
												}
												gameStarting = roomWaitLock.checkIfGameStarting();
											}
											if(!gameStarting){
												
											}
										}
										break;
									   }
				
				case JSONCodes.guesser:{
										WaitingRoomLock 	roomWaitLock;
										String 	userName 		= (String) messageFromClient.get(JSONCodes.userName),
												roomName 		= "";
										Boolean gameStarting 	= false;
										System.out.println("Creating guesser");
										MyRegistry.addAvailableGuesser(userName); // guesser receives available rooms' updates
										
										while(!gameStarting){
											messageFromClient = (JSONObject) new JSONParser().parse(in.readLine());
											roomName = (String) messageFromClient.get(JSONCodes.roomName);
											while(MyRegistry.joinRoom( roomName, userName) == false){
												messageToClient.put(JSONCodes.message, JSONCodes.guesserJoinError);
												messageFromClient = (JSONObject) new JSONParser().parse(in.readLine());
												roomName = (String) messageFromClient.get(JSONCodes.roomName);
											}
											
											roomWaitLock = MyRegistry.getRoomWaitLock(roomName);
											synchronized (roomWaitLock) { 
												/* wait for:	- the master to leave
												 *				- the game to start
												 *				- the timeout to expire
												 */
												while(waitCompletion){
													try {
														roomWaitLock.wait();
														waitCompletion = false;
													} catch (InterruptedException e) {
														e.printStackTrace();
													}
												}
												gameStarting = roomWaitLock.checkIfGameStarting();
											}
											if(!gameStarting){ // room closed because master left
												messageToClient.put(JSONCodes.message, JSONCodes.roomClosed);
												out.println(messageToClient); // notify user to get the new room to join
											}else{ // game starting
												messageToClient.put(JSONCodes.message, JSONCodes.connectionClosed);
												
											}
										}
										
										// guesser has joined the room
										
										break;
									   }
				default: 				System.out.println("Bad JSONCodes.role's content");
										break;
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        	try { // terminate connectionTimeout's alarm to close the socket.
        		connectionTimeout.alarmActivated = false;
        		connectionTimeout.interrupt();
				connectionTimeout.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	System.out.println("joined Timeout Thread");
        	
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
