package userAgent;

import java.io.IOException;
import java.net.InetAddress;
import messages.JSONCodes;

import org.json.simple.JSONObject;

public class MasterWorker {
	private MulticastSocketHandler handler;
	private TargetWord targetWord;
	private char receivedCh;
	private JSONObject messageToGuessers, receivedMessage;
	private int errors, numberOfAttempts;
	private boolean gameFinished = false;
	private MasterTerminationThread terminationThread;
	
	public MasterWorker(String password, String multicastAddr, String targetword, int numberOfAttempts) throws IOException{
		handler = new MulticastSocketHandler(InetAddress.getByName(multicastAddr), 4444, password, 300, JSONCodes.master);
		this.targetWord = new TargetWord(targetword);
		this.numberOfAttempts = numberOfAttempts;
		terminationThread = new MasterTerminationThread(Thread.currentThread());
		terminationThread.start();
	}
	
	@SuppressWarnings("unchecked")
	public void startGame() throws IOException{
		
		while(!gameFinished){
			
			
			receivedMessage = handler.receive();
			if((boolean) receivedMessage.get(JSONCodes.guesserTimeout)){
				break; // The guessers are not sending guesses anymore. They probably all left.
			}
			
			receivedCh = (char)receivedMessage.get(JSONCodes.guess);
			
			messageToGuessers.put(JSONCodes.ack, receivedCh); // acknowledge the current guess
			
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
				messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.masterLeft);
				gameFinished = true;
			}else{
				if(targetWord.isRepeatedGuess(receivedCh)){
					messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.repeatedGuess);
				}else{
					if(targetWord.has(receivedCh)){
						if(targetWord.isGameFinished()){
							messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.masterLost);
							gameFinished = true;
						}else
							messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.correctGuess);
					}else{
						System.out.println(errors++);
						if(errors >= numberOfAttempts){
							messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.masterWon);
							gameFinished = true; 
						}else{
							messageToGuessers.put(JSONCodes.gameStatus, JSONCodes.wrongGuess);
						}
					}
				}
			}
			messageToGuessers.put(JSONCodes.word, targetWord.stringSoFar());
			handler.send(messageToGuessers);
		}
		handler.close();
	}
}
