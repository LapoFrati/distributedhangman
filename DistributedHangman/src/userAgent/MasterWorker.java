package userAgent;

import java.io.IOException;
import java.net.InetAddress;
import messages.JSONCodes;

import org.json.simple.JSONObject;

public class MasterWorker {
	private MulticastSocketHandler handler;
	private TargetWord targetWord;
	private int errors, numberOfAttempts, numberOfGuessers;
	private MasterTerminationThread terminationThread;
	
	public MasterWorker(String password, String multicastAddr, String targetword, int numberOfAttempts, int numberOfGuessers) throws IOException{
		handler = new MulticastSocketHandler(InetAddress.getByName(multicastAddr), 4444, password, 300, JSONCodes.master); // 5 mins timeout
		this.targetWord = new TargetWord(targetword);
		this.numberOfAttempts = numberOfAttempts;
		this.numberOfGuessers = numberOfGuessers;
		terminationThread = new MasterTerminationThread(Thread.currentThread(), handler);
		terminationThread.start();
	}
	
	@SuppressWarnings("unchecked")
	public void startGame() throws IOException{
		JSONObject messageToGuessers, receivedMessage, initializationMessage;
		boolean gameFinished = false, guesserLeft;
		String replyTo, receivedGuess;
		char receivedCh;
		
		messageToGuessers = new JSONObject();
		messageToGuessers.put(JSONCodes.role, JSONCodes.master);
		
		initializationMessage = new JSONObject();
		initializationMessage.put(JSONCodes.role, JSONCodes.master);
		initializationMessage.put(JSONCodes.gameStatus, JSONCodes.initialization);
		initializationMessage.put(JSONCodes.wordHint, targetWord.stringSoFar());
		initializationMessage.put(JSONCodes.attempts, numberOfAttempts);
		handler.send(initializationMessage);
		System.out.println("Initialization message sent");
		
		gameLoop:
		while(!gameFinished){
			
			receivedMessage = handler.masterReceive();
			if(receivedMessage == null){
				break; // The guessers are not sending guesses anymore. They probably all left.
			}
			guesserLeft = (boolean) receivedMessage.get(JSONCodes.guesserLeft);
			if(guesserLeft){
				numberOfGuessers--;
				System.out.println("NumberOfGuessers: "+numberOfGuessers);
				if(numberOfGuessers == 0){
					System.out.println("All guessers left. The game is over.");
					terminationThread.terminate();
					break gameLoop;
				}
			}else{
				receivedGuess 	= (String)receivedMessage.get(JSONCodes.guess);
				replyTo 		= (String)receivedMessage.get(JSONCodes.senderNick);
				receivedCh 		= receivedGuess.charAt(0);
				
				messageToGuessers.put(JSONCodes.ack, receivedGuess); // acknowledge the current guess
				messageToGuessers.put(JSONCodes.replyTo, replyTo); // specify the target guesser
				
				/*
				 * Update game status:	
				 * 						- masterLeft
				 * 						- repeatedGuess
				 * 						- masterLost
				 * 						- corretGuess
				 * 						- masterWon
				 * 						- wrongGuess
				 */
				
				if(Thread.interrupted()){
					// master has left
					break gameLoop;
				}else{
					if(targetWord.isRepeatedGuess(receivedCh)){
						messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.repeatedGuess);
						messageToGuessers.put(JSONCodes.previousGuesses, targetWord.guessesSoFar());
					}else{
						if(targetWord.has(receivedCh)){
							if(targetWord.isGameFinished()){
								messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.masterLost);
								gameFinished = true;
								terminationThread.terminate();
							}else{
									messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.correctGuess);
									messageToGuessers.put(JSONCodes.previousGuesses, targetWord.guessesSoFar());
								}
						}else{
							System.out.println(++errors);
							if(errors >= numberOfAttempts){
								messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.masterWon);
								gameFinished = true; 
								terminationThread.terminate();
							}else{
								messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.wrongGuess);
								messageToGuessers.put(JSONCodes.previousGuesses, targetWord.guessesSoFar());
							}
						}
					}
				}
				messageToGuessers.put(JSONCodes.word, targetWord.stringSoFar());
				handler.send(messageToGuessers);
			}
		}
	}
}
