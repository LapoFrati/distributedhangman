package registry;

import java.io.IOException;
import java.net.Socket;

import messages.JSONCodes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterCreationThread extends AbstractGameCreation{
	private String 	roomName = "";
	private WaitingRoom waitingRoom;
	private WaitingRoomLock roomWaitLock;
	private Thread toInterrupt;
	
	public MasterCreationThread(Socket socket, long timeout){
		super(socket, timeout);
		roomName = "";
		roomWaitLock = new WaitingRoomLock();
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run(){

        try {
			JSONObject 	messageFromClient 	= (JSONObject) new JSONParser().parse(in.readLine()),
						messageToClient 	= new JSONObject();
			String	password 		= "",
					multicastAddr 	= "";
			 
			int 	requiredGuessers 	= ((Long) messageFromClient.get(JSONCodes.numberOfGuessers)).intValue();
			/*String*/		roomName 	= (String) messageFromClient.get(JSONCodes.roomName);
			boolean gameStarting = false;

			
			/* Master Protocol:
			 * 
			 * 1. Get room and numberOfGuessers
			 * 
			 * 2. Create room 
			 * 
			 * 3. Start additional threads		
			 * 
			 * 4. Wait for game to start	4.1 -if( exitReceived) then END.
			 * 								4.2 |if( connTimeout ) then send connClosed and END.
			 * 
			 * 5. Game start
			 * 
			 * 6. END
			 * 
			 */
			
			Thread.currentThread().setName(roomName); 		// for easier debug
			System.out.println("New Master: " + roomName);	// for the master the userName and roomName are the same
			toInterrupt = Thread.currentThread();			// set thread to interrupt
			
			// Since the fixedPool's size for the masters is 10, there can't be more than 10 master active simultaneously -> no more than 10 rooms
			waitingRoom = new WaitingRoom(roomName, requiredGuessers, roomWaitLock);
			MyRegistry.addNewWaitingRoom(roomName, requiredGuessers, waitingRoom);		
			
			connectionTimeout.start();// start timeout to close the socket
			exitListener.start(); // start the thread to listen for an exit request

			synchronized (roomWaitLock) {	
				try {
					roomWaitLock.wait();
				} catch (InterruptedException e) {
					System.out.println("interrupt received");
					// something happened, either exitReceived or timeoutExpired, stop the other one
					if( exitReceived == true ){
						stopTimeout();
					}
					if( timeoutExpired == true ){
						stopListener();
						out.println(closingJSON);
					}
					roomWaitLock.notifyAll(); // Since the lock is not set to gameStarting the guessers will leave
				}
				
				if(gameStarting = roomWaitLock.checkIfGameStarting()){ // if game is not starting the master has been woken up by its threads
					stopTimeout();
					stopListener();
					password		= roomWaitLock.getPassword();
					multicastAddr 	= roomWaitLock.getMulticast();
				}
				
				MyRegistry.closeWaitingRoom(roomName, waitingRoom);
			}
			if( !exitReceived && !timeoutExpired ){
				if(gameStarting){
					// send information needed to start the game
					messageToClient.put(JSONCodes.message, JSONCodes.gameStarting);
					messageToClient.put(JSONCodes.roomPassword, password);
					messageToClient.put(JSONCodes.roomMulticast, multicastAddr);
					out.println(messageToClient);
				}
			}
			
        } catch (ParseException e) {
			e.printStackTrace();
		} catch (IOException e) {
            e.printStackTrace();
        } finally {
        	try {
        		if(!socket.isClosed())
        			socket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
	
	public synchronized void notifyExit() {
		exitReceived = true;
		toInterrupt.interrupt();
	}

	public synchronized void notifyTimeout() {
		timeoutExpired = true;
		toInterrupt.interrupt();
	}
}
