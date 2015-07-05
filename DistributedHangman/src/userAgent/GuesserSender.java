package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import org.json.simple.JSONObject;

import messages.JSONCodes;

public class GuesserSender extends Thread{
	BufferedReader stdIn;
	String guess, userName;
	MulticastSocketHandler handler;
	GuessACK guessACK;
	long timeoutSeconds;
	boolean keepGoing, exitReceived;
	
	public GuesserSender(String userName, MulticastSocketHandler handler, GuessACK guessACK, long timeout){
		this.userName = userName;
		this.handler = handler;
		this.guessACK = guessACK;
		this.timeoutSeconds = timeout*1000;
		stdIn = new BufferedReader( new InputStreamReader(System.in) );
		keepGoing = true;
		exitReceived = false;
	}
	
	@SuppressWarnings("unchecked")
	public void run(){
		JSONObject messageToMaster = new JSONObject();
		
		// initialize fixed fields in the json used for communication
		messageToMaster.put(JSONCodes.role, JSONCodes.guesser);
		messageToMaster.put(JSONCodes.senderNick, userName);
		messageToMaster.put(JSONCodes.guesserLeft, false);
		
		while(keepGoing){
			
			guess = getGuess();
			
			
			if(keepGoing == true){
				
				messageToMaster.put(JSONCodes.guess, guess);
				
				synchronized (guessACK) {
					do{
						try {
							handler.send(messageToMaster);
						} catch (IOException e) {
							e.printStackTrace();
						}
						
						try {
							guessACK.wait(timeoutSeconds);
						} catch (InterruptedException e) {
							// interrupt is sent when the game is finished and the sender thread has to terminate
							keepGoing = false;
							break;
						}
					}while(guessACK.testAndResetACK() == false);
				}
			}
		}
		
		if(exitReceived == true){ // if the guesser is leaving notify master

			messageToMaster.remove(JSONCodes.guess); // remove the unused field to save space
			messageToMaster.put(JSONCodes.guesserLeft, true);
			
			try {
				handler.send(messageToMaster);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}
		
		try {
			handler.close(); // close the socket to stop the receiver thread
		} catch (IOException e) {
			e.printStackTrace();
		}
		
	}
	
	protected String getGuess(){
		String userInput = null;
		String result = "-";
		boolean commandAccepted = false;
		
		getInputLoop:
		while(!commandAccepted){
			System.out.println("Guess: ");
			try {
				userInput = stdIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			if(userInput != null){ // check if the readLine has not been interrupted by System.in.close
				if(userInput.matches("[a-z]")){ // check input legality
					commandAccepted = true;
					result = userInput;
				}else{
					if(userInput.matches("exit")){ // check termination request
						commandAccepted = true;
						keepGoing = false;
						exitReceived = true;
						System.out.println("You left. GAME OVER.");
					}
					else
						System.out.println("Wrong input, must be a single letter [a-z] or \"exit\".");
				}
			} else {
				// readLine has been interrupted, stop trying to get input
				break getInputLoop;
			}
		}
		return result;
	}
	
	public void terminate() throws IOException{
		keepGoing = false;
		// the sender thread might be getting a new guess or waiting for an ack. We cover both cases
		System.in.close(); // interrupts the sender's getGuess
		this.interrupt(); // stop the sender thread waiting for the ack
	}

}
