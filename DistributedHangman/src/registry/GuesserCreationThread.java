package registry;

import java.io.IOException;
import java.net.Socket;

import messages.JSONCodes;

import org.json.simple.JSONObject;

public class GuesserCreationThread extends AbstractGameCreation{
	private String roomName, userName;
	private WaitingRoomLock roomWaitLock;
	private Thread toInterrupt;
	
	public GuesserCreationThread(Socket socket, long timeout){
		super(socket, timeout);
		this.socket = socket;
		this.timeout = timeout;
		this.roomName = "";
		this.userName = "";
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		try{
			JSONObject 	messageToClient 	= new JSONObject();
			String 	password 		= "",
					multicastAddr 	= "";
			Boolean gameStarting 	= false;
			
			connectionTimeout.start(); // start timeout
			
			/*	Guesser Protocol
			 * 
			 * 	1. Get userName
			 * 
			 *  2. Send rooms' info to the guesser 
			 * 
			 * 	3. Get roomTo join:	3.1	|if( timeout) then send JSON(connectionClosed) and END
			 * 						3.2 -if( exit	) then END
			 * 
			 * 	4. Join room	4.1	-if( joinError		) then send JSON(joinError) and go back to 3.
			 * 
			 * 	5. Wait Game Start 	5.1	-if( gameStart		) then send JSON(pass, multicast) and END
			 * 						5.2	|if( masterLeave	) then send JSON(roomClosed) and END
			 * 						5.3	|if( timeout		) then leave room, send JSON(connectinClosed) and END
			 * 						5.4 |if( exitReceived	) then leave room and END
			 *  6. END
			 * 
			 */
			
			// 1. Get userName
			userName = in.readLine();
			Thread.currentThread().setName(userName); // easier debug
			System.out.println("New Guesser: "+userName); 
			toInterrupt = Thread.currentThread();			// set thread to interrupt
			
			// 2. Send rooms' info to the guesser
			MyRegistry.addAvailableGuesser(userName); // log guesser to receive available rooms' updates
			
			while(!gameStarting){
				
				while(MyRegistry.joinRoom( roomName = in.readLine() /* 3. */ ) == false){ // room could be full or the name could be wrong
					
					// 4.1 ( joinError )
					messageToClient.put(JSONCodes.message, JSONCodes.guesserJoinError);
					out.println(messageToClient);
				}
				
				// notify room joined
				messageToClient.put(JSONCodes.message, JSONCodes.roomJoined);
				out.println(messageToClient);
				
				// 4. Here room is joined
				exitListener.start(); // start the thread to listen for an exit request
				
				roomWaitLock = MyRegistry.getRoomWaitLock(roomName);
				
				// 5. Wait game start
				synchronized (roomWaitLock) { 
					/* wait for:	- the master to leave
					 *				- the game to start
					 *				- the timeout to expire 				(separate thread, use interrupt)
					 *				- the guesser to send and exit command 	(separate thread, use interrupt)
					 */
					
						if(gameStarting = roomWaitLock.checkIfGameStarting()){ 
							// the last guesser to join doesn't have to wait, checkIfGameStart notifies users if needed
							stopTimeout();
							password		= roomWaitLock.getPassword();
							multicastAddr 	= roomWaitLock.getMulticast();
						} else {
							// guesser was not the last one, wait for more guessers to join
							try {
								roomWaitLock.wait();
							} catch (InterruptedException e) {
								// 5.3 (timeout expired or exit received)
								if( exitReceived == true ){
									stopTimeout();
								}
								if( timeoutExpired == true ){
									stopListener();
									out.println(closingJSON);
								}
								MyRegistry.leaveRoom(roomName);
								MyRegistry.removeAvailableGuesser(userName);
							}
						}													

					if(gameStarting = roomWaitLock.checkIfGameStarting()){
						stopListener();
						stopTimeout();
						password		= roomWaitLock.getPassword();
						multicastAddr 	= roomWaitLock.getMulticast();
					}
				}
				if(!exitReceived && !timeoutExpired){
					if(gameStarting){
						// 5.1
						messageToClient.put(JSONCodes.message, JSONCodes.gameStarting);
						messageToClient.put(JSONCodes.roomPassword, password);
						messageToClient.put(JSONCodes.roomMulticast, multicastAddr);
						out.println(messageToClient); // send info to start game
					} else {
						// 5.2 or 5.3 (masterLeft or timeoutExpired)
						messageToClient.put(JSONCodes.message, JSONCodes.roomClosed);
						out.println(messageToClient);
					}
				}
			}
			// game is starting
	        	
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
