package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.net.UnknownHostException;

import messages.JSONCodes;

import org.json.simple.JSONObject;

public class Guesser {
	String userName, password, multicast, gameStatus;
	MulticastSocketHandler handler;
	BufferedReader stdIn;
	
	public Guesser(String userName, String password, String multicastAddr) throws UnknownHostException, IOException{
		this.userName = userName;
		this.password = password;
		stdIn = new BufferedReader( new InputStreamReader(System.in) );
		handler = new MulticastSocketHandler(InetAddress.getByName(multicastAddr), 4444, password,30, JSONCodes.guesser);
	}
	
	@SuppressWarnings("unchecked")
	public void startGame() throws IOException{
		
		boolean gameFinished = false, ackReceived, checkAck;
		JSONObject messageToMaster = new JSONObject(), messageFromMaster;
		char guess;
		
		while(!gameFinished){
			
			guess = getGuess();
			messageToMaster.put(JSONCodes.guess, guess);
			handler.send(messageToMaster);
			
			ackReceived = checkAck = false;
			
			ackLoop: // label used to break out of loop if we end up in a game-ending state
			while(!ackReceived){
				
				messageFromMaster = handler.receive();
				
				if((boolean)messageFromMaster.get(JSONCodes.guesserTimeout)){
					// if the timeout expires start a new transaction
					break ackLoop;
				}

				gameStatus = (String) messageFromMaster.get(JSONCodes.gameStatus);
				
				// check the game-ending statuses
				switch(gameStatus){
					case JSONCodes.masterLeft:		System.out.println("The master has left. Guessers win the game.");
													gameFinished = true;
													break ackLoop;
					case JSONCodes.masterWon:		System.out.println("The master wins.");
													gameFinished = true;
													break ackLoop;
					case JSONCodes.masterLost:		System.out.println("The word has been guessed. Guessers win the game.");
													gameFinished = true;
													break ackLoop;
					default:						checkAck = true; // non game-ending status received
													break;
				}
				
				// check if the ACK is the same as our guess
				if( checkAck && ((char) messageFromMaster.get(JSONCodes.guess)) == guess){
					ackReceived = true;
					// check the type of ACK we received
					switch(gameStatus){
						case JSONCodes.correctGuess:	System.out.println("Correct guess.");
														break;
						case JSONCodes.repeatedGuess:	System.out.println("Your guess: "+guess+" had already been made.");
														break;
						case JSONCodes.wrongGuess:		System.out.println("Wrong guess.");
														break;
					}
				}
			}
		}
	}
	
	private char getGuess() throws IOException{
		String userInput;
		char result = 0;
		boolean guessAccepted = false;
		
		while(!guessAccepted){
			System.out.println("Guess: ");
			userInput = stdIn.readLine();
			if(userInput.matches("[a-z]")){ // check input legality
				guessAccepted = true;
				result = userInput.toCharArray()[0]; // convert the one letter string to a char
			}else{
				System.out.println("Wrong input, must be a single letter.");
			}
		}
		
		return result;
	}
	
}
