package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.net.UnknownHostException;

import messages.JSONCodes;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MasterLauncher extends AbstractUserLauncher{
	BufferedReader stdIn;
	String userName, masterInterface;
	boolean exitReceived;
	
	public MasterLauncher(BufferedReader stdIn, String userName, String serverIP, String masterServerPort, String masterInterface) {
		super(stdIn, serverIP, masterServerPort);
		this.stdIn = stdIn;
		this.userName = userName;
		this.masterInterface = masterInterface;
		exitReceived = false;
	}

	@SuppressWarnings("unchecked")
	public void startMaster() throws NumberFormatException, UnknownHostException, IOException{
		JSONObject 	messageToServer, 
					messageFromServer = null;
		
    	String 	password 	= "", 
    			multicast 	= "",
    			targetword 	= "",
				message		= "";
    	
    	int 	numberOfGuessers = -1,
    			numberOfAttempts = -1;
		
    	boolean readNumber 	= true,
				gameStarting = true;
    	
    	try {
				messageToServer = new JSONObject();
				
				// get number of guessers
        		System.out.println("Required number of guessers: ");
        		while(readNumber){
        			try{
        			numberOfGuessers = Integer.valueOf(stdIn.readLine());
        			readNumber = false;
        			}catch (NumberFormatException e) {
						System.out.println("Wrong number format.");
					}
        		}
        		
        		// send necessary info to create a waitingRoom
        		messageToServer.put(JSONCodes.roomName, userName);
        		messageToServer.put(JSONCodes.numberOfGuessers, numberOfGuessers);
        		out.println(messageToServer);
				
        		System.out.println("Request sent");
        		
        		// start the exitListener thread
        		startListener();
        		
    			try{
    				message = in.readLine();
    			} catch (IOException e){
    				// notifyExit closes in to stop the readline
    			}
    			
    			checkState(); // Throws TerminationException
    			
        		try {
					messageFromServer = (JSONObject) new JSONParser().parse(message);	
				} catch (ParseException e) {
					e.printStackTrace();
				}
        		
    			switch((String) messageFromServer.get(JSONCodes.message)){
    				case JSONCodes.gameStarting:		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
	        											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
	        											gameStarting = true;
	        											System.out.println("Game is starting");
	        											break;	
    				case JSONCodes.connectionClosed: 	System.out.println("Connection closed. Game aborted"); // server side timeout expired
    													gameStarting = false;
    													break;
    			}
        		
        		
    	} catch (TerminationException e){
    		//System.out.println(e.getMessage());
    		/* CheckStates has forced the program to exit the loop throwing this exception */
    	} finally {
    		stopListener(); // close the listener if no exit request has been received
    		if(!serverSocket.isClosed())
    			serverSocket.close();
    	}
    	
    	if(gameStarting && !exitReceived){
    		System.out.println(password + "-" + multicast);
    		targetword = getWord();
			numberOfAttempts = getAttempts();
			MasterWorker master = new MasterWorker(password, multicast, targetword, numberOfAttempts, numberOfGuessers, masterInterface);
	    	master.startGame();
		}  
	}
    
	/**
	 * Method used to get a legal word from the master.
	 * @return the word to guess.
	 */
	public String getWord(){
    	String targetWord = "", reply = "";
    	boolean proceed = false;
    	
    	System.out.println("Choose the target word");
		do{
			try {
				targetWord = stdIn.readLine().toLowerCase();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(targetWord.length() == 0){
				System.out.println("Choose the target word");
				continue;
			}
			System.out.println("The chosen word is: " + targetWord+".");
			do{
				System.out.println("Proceed? [Y/N]");
				try {
					reply = stdIn.readLine();
				} catch (IOException e) {
					e.printStackTrace();
				} 
			}while(!reply.matches("y|Y|n|N"));
			
			if(reply.matches("n|N")){
				proceed = false;
				System.out.println("Choose the target word");
			} else {
				proceed = true;
			}
		} while(!proceed);
		
		return targetWord;
    }
    
	/**
	 * Method used to get the number of attempts allowed.
	 * @return the number of attempts allowed
	 */
    public int getAttempts(){
    	Boolean attemptsOk = false;
    	int numberOfAttempts = 0;
    	String reply = ""; 
    	
    	System.out.println("Choose number of allowed attempts:");
		while(!attemptsOk){
			try {
				numberOfAttempts = Integer.valueOf(stdIn.readLine());
			} catch (Exception e) {
				System.out.println("Enter only numbers, please.");
				continue;
			}
			if(numberOfAttempts > 0 ){
				System.out.println("The chosen number of attempts is: " + numberOfAttempts+".");
				do{
					System.out.println("Proceed? [Y/N]");
					try {
						reply = stdIn.readLine();
					} catch (IOException e) {
						e.printStackTrace();
					} 
				}while(!reply.matches("y|Y|n|N"));
				
				if(reply.matches("n|N")){
					attemptsOk = false;
					System.out.println("Choose number of allowed attempts:");
				} else {
					attemptsOk = true;
				}
			} else {
				System.out.println("Enter a number greater than 0.");
				attemptsOk = false;
			}
		}
		
		return numberOfAttempts;
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
			throw new TerminationException("MasterLauncher received exit request.");
		}
	}
}
