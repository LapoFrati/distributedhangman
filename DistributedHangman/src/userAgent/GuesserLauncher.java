package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import messages.JSONCodes;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class GuesserLauncher {
	@SuppressWarnings("unchecked")
	public void startGuesser(BufferedReader stdIn, String userName, String serverIP, String guesserServerPort) throws NumberFormatException, UnknownHostException, IOException{
		String 	password 	= "", 
    			multicast 	= "";
		Boolean keepReading = true;
		JSONObject messageToServer, messageFromServer = null;
		messageToServer = new JSONObject();
		messageToServer.put(JSONCodes.role, JSONCodes.guesser);
		messageToServer.put(JSONCodes.userName, userName);
		
		try (	// Create a socket to connect to the server using the info in the configuration file
                Socket serverSocket = new Socket(InetAddress.getByName(serverIP), Integer.parseInt(guesserServerPort));
                
        		// TCP socket provides bidirectional I/O. Attach in/out to the respective streams.
        		PrintWriter out =
                    new PrintWriter(serverSocket.getOutputStream(), true);
                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
            ){
			out.println(messageToServer);
			
			System.out.println("Choose room to join: ");
			messageToServer.put(JSONCodes.roomName, stdIn.readLine());
			out.println(messageToServer);
			while(keepReading){
				try {
					messageFromServer = (JSONObject) new JSONParser().parse(in.readLine());
					
				} catch (ParseException e) {
					System.out.println("Message:" + messageFromServer);
					e.printStackTrace();
				}
				switch((String) messageFromServer.get(JSONCodes.message)){
				
					case JSONCodes.roomJoined: 			System.out.println("Joined selected room, please wait for game to start");
														break;
					case JSONCodes.connectionClosed:	System.out.println("Game is starting");
														keepReading = false;
														break;
					case JSONCodes.gameStarting: 		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
		    											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
		    											break;
					case JSONCodes.roomClosed:			System.out.println("Room closed.");
					case JSONCodes.guesserJoinError:	System.out.println("Choose room to join: ");
														messageToServer.put(JSONCodes.roomName, stdIn.readLine());
														out.println(messageToServer);
		    											break;
				}		
			} 
		} catch (UnknownHostException e) {
            System.out.println("Don't know about host " + InetAddress.getByName(serverIP));
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " +
            		InetAddress.getByName(serverIP));
        }
		System.out.println(password + "-" + multicast);
		GuesserReceiver guesser = new GuesserReceiver(userName, password,multicast);
		guesser.startGame();
	}
}
