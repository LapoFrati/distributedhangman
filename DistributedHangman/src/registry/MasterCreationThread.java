package registry;

import java.io.IOException;
import java.net.Socket;

import messages.JSONCodes;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterCreationThread extends AbstractGameCreation{
	private boolean exitReceived, timeoutExpired;
	private String 	roomName = "";
	private WaitingRoom waitingRoom;
	private WaitingRoomLock roomWaitLock;
	
	public MasterCreationThread(Socket socket, long timeout){
		super(socket, timeout);
		exitReceived = false;
		timeoutExpired = false;
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

			
			/* Master Protocol:
			 * 
			 * 1. Get room and numberOfGuessers
			 * 
			 * 2. Start additional threads
			 * 
			 * 3. Create room				3.1 -if( exitReceived) then END.
			 * 								3.2 |if( connTimeout ) then send connClosed and END.
			 * 
			 * 4. Wait for game to start	4.1 -if( exitReceived) then END.
			 * 								4.2 |if( connTimeout ) then send connClosed and END.
			 * 
			 * 5. Game start
			 * 
			 * 6. END
			 * 
			 */
			
			connectionTimeout.start();// start timeout to close the socket
			exitListener.start(); // start the thread to listen for an exit request
			
			
			Thread.currentThread().setName(roomName); // for easier debug
			System.out.println("New Master: " + roomName); // for the master the userName and roomName are the same
			
			// Since the fixedPool's size for the masters is 10, there can't be more than 10 master active simultaneously -> no more than 10 rooms
			waitingRoom = new WaitingRoom(roomName, requiredGuessers, roomWaitLock);
			MyRegistry.addNewWaitingRoom(roomName, requiredGuessers, waitingRoom);		
			
			checkState(); /* 3.1 & 3.2 */
			
			// 3.
			messageToClient.put(JSONCodes.message, JSONCodes.newRoomCreated);	
			out.println(messageToClient);
			
			checkState();
			
			synchronized (roomWaitLock) {	
				try {
					roomWaitLock.wait();
				} catch (InterruptedException e) {
					//if master gets interrupted the timeout has finished
					roomWaitLock.notifyAll(); // wake up all guessers
				}
				
				if(roomWaitLock.checkIfGameStarting()){ // if game is not starting the master has been woken up by its thread
					stopTimeout();
					stopListener();
					password		= roomWaitLock.getPassword();
					multicastAddr 	= roomWaitLock.getMulticast();
					MyRegistry.closeWaitingRoom(roomName, waitingRoom); // close room to stop new guessers
				}
			}

			checkState();
			
			// send information needed to start the game
			messageToClient.put(JSONCodes.message, JSONCodes.gameStarting);
			messageToClient.put(JSONCodes.roomPassword, password);
			messageToClient.put(JSONCodes.roomMulticast, multicastAddr);
			out.println(messageToClient);
			
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

	@Override
	public synchronized void notifyExit() {
		exitReceived = true;
		MyRegistry.closeWaitingRoom(roomName, waitingRoom); // stops new users from joining it
		roomWaitLock.notifyAll(); // Since the lock is not set to gameStarting the guessers will leave
	}

	@Override
	public synchronized void notifyTimeout() {
		timeoutExpired = true;
		MyRegistry.closeWaitingRoom(roomName, waitingRoom); // stops new users from joining the room
		roomWaitLock.notifyAll(); // Since the lock is not set to gameStarting the guessers will leave
	}

	@SuppressWarnings("unchecked")
	@Override
	public synchronized void checkState() {
		if( exitReceived == true ){
			stopTimeout();
		}
		
		if( timeoutExpired == true ){
			stopListener();
			JSONObject closingJSON = new JSONObject();
			closingJSON.put(JSONCodes.message, JSONCodes.connectionClosed);
			out.println(closingJSON);
		}
		
		if( exitReceived == true || timeoutExpired == true){
			System.exit(0);
		}
	}
}
