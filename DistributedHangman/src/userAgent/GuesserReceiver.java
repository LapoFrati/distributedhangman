package userAgent;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import messages.JSONCodes;

import org.json.simple.JSONObject;

public class GuesserReceiver {
	String userName, password, multicast, gameStatus;
	MulticastSocketHandler handler;
	GuesserSender guesserSender;
	GuessACK guessACK;
	
	public GuesserReceiver(String userName, String password, String multicastAddr, String guesserInterface) throws UnknownHostException, IOException{
		this.userName = userName;
		this.password = password;
		guessACK = new GuessACK();
		handler = new MulticastSocketHandler(InetAddress.getByName(multicastAddr), 4444, password,300, JSONCodes.guesser, guesserInterface); // 5 mins timeout
		guesserSender = new GuesserSender(userName, handler, guessACK, 30);
	}
	
	public void startGame() throws IOException{
		JSONObject messageFromMaster;
		String word, guessesSoFar;
		
		
		System.out.println("Initializing game. Please wait.");

		gameLoop:
		while(true){
			
			if( (messageFromMaster = handler.guesserReceive()) == null){
				break gameLoop;
			}
	
			gameStatus = (String) messageFromMaster.get(JSONCodes.gameStatus);
			
			// check the game-ending statuses
			switch(gameStatus){
				case JSONCodes.masterLeft:		System.out.println("The master has left. Guessers win the game.");
												guesserSender.terminate();
												break gameLoop;
				case JSONCodes.masterWon:		System.out.println("The master wins.");
												guesserSender.terminate();
												break gameLoop;
				case JSONCodes.masterLost:		word = (String) messageFromMaster.get(JSONCodes.word);
												System.out.println("The word has been guessed: "+word+". Guessers win the game.");
												guesserSender.terminate();
												break gameLoop;
				case JSONCodes.initialization:	System.out.println("["+messageFromMaster.get(JSONCodes.wordHint)+"]");
												System.out.println("Allowed attempts: "+messageFromMaster.get(JSONCodes.attempts));
												guesserSender.start();
												continue gameLoop;
				default:						break; // non game-ending status received, continue with the checks		
			}
			
			// check if we are the message's recipient
			if(((String) messageFromMaster.get(JSONCodes.replyTo)).equals(userName)){
				synchronized (guessACK) {
					guessACK.setACK();
					guessACK.notify(); // wake up the GuesserSender waiting for the ack
				}
				// check the outcome of our guess
				switch(gameStatus){
					case JSONCodes.correctGuess:	System.out.println("Your guess was correct.");
													word = (String) messageFromMaster.get(JSONCodes.word);
													System.out.println("["+word+"]");
													break;
					case JSONCodes.repeatedGuess:	System.out.println("Your guess had already been made.");
													break;
					case JSONCodes.wrongGuess:		System.out.println("Wrong guess.");
													break;
				}
				
				guessesSoFar = (String) messageFromMaster.get(JSONCodes.previousGuesses);
				System.out.println("GuessesSoFar: "+guessesSoFar);
				
			} else {
				// the message was not targeted to us. check if it contains useful information.
				if(gameStatus.equals(JSONCodes.correctGuess)){
					System.out.println("New correct guess:");
					word = (String) messageFromMaster.get(JSONCodes.word);
					System.out.println("["+word+"]");
					guessesSoFar = (String) messageFromMaster.get(JSONCodes.previousGuesses);
					System.out.println("GuessesSoFar: "+guessesSoFar);
				}
			}
		}
	}
}
