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

public class MasterLauncher {
	@SuppressWarnings("unchecked")
	public void startMaster(BufferedReader stdIn, String userName, String serverIP, String masterServerPort) throws NumberFormatException, UnknownHostException, IOException{
		JSONObject 	messageToServer, 
					messageFromServer = null;
		
    	String 	password 	= "", 
    			multicast 	= "",
    			targetword 	= "",
				reply 		= "";
    	
    	int 	numberOfGuessers = -1,
    			numberOfAttempts = -1;
		
    	boolean readNumber 	= true,
				keepReading = true,
				attemptsOk 	= false,
				proceed 	= false;
    	
    	Thread terminationThread;
    	
    	try (	// Create a socket to connect to the server using the info in the configuration file
                Socket serverSocket = new Socket(InetAddress.getByName(serverIP), Integer.parseInt(masterServerPort));
                
        		// TCP socket provides bidirectional I/O. Attach in/out to the respective streams.
        		PrintWriter out =
                    new PrintWriter(serverSocket.getOutputStream(), true);
                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
            ){
    				messageToServer = new JSONObject();
    				terminationThread = new MasterLauncherTerminationThread(out, stdIn, in);
		        	
	        		System.out.println("Required number of guesser: ");
	        		while(readNumber){
	        			try{
	        			numberOfGuessers = Integer.valueOf(stdIn.readLine());
	        			readNumber = false;
	        			}catch (NumberFormatException e) {
							System.out.println("Wrong number format.");
						}
	        		}
    				messageToServer.put(JSONCodes.role, JSONCodes.master);
	        		messageToServer.put(JSONCodes.roomName, userName);
	        		messageToServer.put(JSONCodes.numberOfGuessers, numberOfGuessers);
	        		
	        		System.out.println("Sending room info");
	        		
	        		out.println(messageToServer);
					
	        		System.out.println("Waiting for reply");
	        		
	        		// start the termination thread
	        		terminationThread.run();
	        		
	        		while(keepReading){
		        		try {
							messageFromServer = (JSONObject) new JSONParser().parse(in.readLine());	
						} catch (ParseException e) {
							e.printStackTrace();
						}
	        			switch((String) messageFromServer.get(JSONCodes.message)){
	        				case JSONCodes.waitingRoomsFull: 	System.out.println("Waiting to create a new room");
	        													break;
	        				case JSONCodes.newRoomCreated:		System.out.println("New room created. Waiting for guessers.");
	        													break;
	        				case JSONCodes.gameStarting:		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
			        											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
			        											break;	
	        				case JSONCodes.connectionClosed: 	keepReading = false;
	        													System.out.println("Connection closed.");
	        													break;
	        			}
	        		}
	        		
	        		System.out.println(password + "-" + multicast);
	        		
        			System.out.println("Starting Master");
        			
        			System.out.println("Choose the target word");
        			
        			do{
        				targetword = stdIn.readLine();
        				System.out.println("The chosen word is: " + targetword+".");
        				do{
        					System.out.println("Proceed? [Y/N]");
        					reply = stdIn.readLine(); 
        				}while(reply.matches("y|Y|n|N"));
        				
        				if(reply.matches("n|N")){
        					proceed = false;
        				} else {
        					proceed = true;
        				}
        			}
        			while(!proceed);
        			
        			System.out.println("Choose number of allowed attempts.");
        			while(!attemptsOk){
        				try {
        					numberOfAttempts = Integer.valueOf(stdIn.readLine());
						} catch (Exception e) {}
        				if(numberOfAttempts >0 ){
        					attemptsOk = true;
        				} else {
        					attemptsOk = false;
        				}
        			}
    	} catch (UnknownHostException e) {
            System.out.println("Don't know about host " + InetAddress.getByName(serverIP));
        } catch (IOException e) {
            System.out.println("Couldn't get I/O for the connection to " +
            		InetAddress.getByName(serverIP));
        }
        			MasterWorker master = new MasterWorker(password, multicast, targetword, numberOfAttempts, numberOfGuessers);
        			master.startGame();
    			  
	}
	
}
