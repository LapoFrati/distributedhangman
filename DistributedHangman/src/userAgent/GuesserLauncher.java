package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.SocketException;
import java.net.UnknownHostException;

import messages.JSONCodes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GuesserLauncher extends AbstractUserLauncher{
	BufferedReader stdIn;
	String userName, guesserInterface;
	boolean exitReceived;
	
	public GuesserLauncher(BufferedReader stdIn, String userName,String serverIP, String masterServerPort, String guesserInterface) {
		super(stdIn, serverIP, masterServerPort);
		this.stdIn = stdIn;
		this.userName = userName;
		this.guesserInterface = guesserInterface;
		exitReceived = false;
	}

	public void startGuesser() throws NumberFormatException, UnknownHostException, IOException{
		String 	password 	= "", 
    			multicast 	= "",
    			roomToJoin	= "", 
    			rawMessage	= "";
		Boolean gameStarting = false;
		JSONObject messageFromServer = null;
		
		try{

			out.println(userName); // send userName to start receiving WaitingRooms' updates
			
			System.out.println("Choose room to join: ");
			roomToJoin = stdIn.readLine();
			out.println(roomToJoin);
			
			initializationLoop:
			while(true){
				try{
				rawMessage = in.readLine();
				//System.out.println(rawMessage);
				} catch (SocketException e){
					// notifyExit closes in to stop the readline
				}
				checkState();
				
				if(rawMessage != null){
					try {
						messageFromServer = (JSONObject) new JSONParser().parse(rawMessage);
					} catch (ParseException e) {
						System.out.println("Message:" + messageFromServer);
						e.printStackTrace();
					}
				} else {
					System.out.println("null read");
					break;
				}
				
				checkState();
				
				switch((String) messageFromServer.get(JSONCodes.message)){
					case JSONCodes.roomJoined:			System.out.println("Room joined. Waiting for the game to start. Type \"exit\" to quit.");
														exitListener.start(); 
														continue initializationLoop;
					case JSONCodes.connectionClosed:	System.out.println("Timeout expired. Game aborted.");
														break initializationLoop;
					case JSONCodes.gameStarting: 		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
		    											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
		    											gameStarting = true;
		    											break initializationLoop;
					case JSONCodes.roomClosed:			System.out.println("Room closed. Game aborted.");
														break initializationLoop;
					case JSONCodes.guesserJoinError:	System.out.println("Couldn't join the room.\nPlease choose another one: ");
														roomToJoin = stdIn.readLine();
														out.println(roomToJoin);
		    											continue initializationLoop;
				}		
			} 
		} catch ( TerminationException e ) {
			
		} finally {
			stopListener();
    		if(!serverSocket.isClosed())
    			serverSocket.close();
		}
		
		if(gameStarting && !exitReceived){
			System.out.println(password + "-" + multicast);
			GuesserReceiver guesser = new GuesserReceiver(userName, password, multicast, guesserInterface);
			guesser.startGame();
		}
	}

	@Override
	public void notifyExit() {
		exitReceived = true;
		out.println("exit"); // send closing message to the server
		stopListener(); // prevent further notifications
		try {
			serverSocket.close(); // stops the possible wait on in.readLine
		} catch (IOException e) {
			e.printStackTrace();
		} 
	}

	@Override
	public void checkState() {
		if(exitReceived == true){
			System.out.println("Game Terminated.");
			throw new TerminationException("GuesserLauncher received exit request.");
		}
	}
}
