package userAgent;

import java.util.ArrayList;

public class TargetWord {
	char[] target;
	ArrayList<Character> previousGuesses;
	boolean[] letters;
	int lettersLeft;
	StringBuilder sb;
	
	public TargetWord(String target){
		lettersLeft = 0;
		this.target = target.toCharArray();
		
		/* Array accessed by the char value. Contains true if the letters is in the word 
		 * and has not been guessed yet.
		 */
		letters = new boolean[26]; 
		
		for(char ch : this.target){
			if(letters[ch - 'a'] == false){
				letters[ch - 'a'] = true;
				lettersLeft++; // counts how many different letters there are, used to determine when the word has been completely guessed
			}
		}
		sb = new StringBuilder();
		previousGuesses = new ArrayList<Character>();
	}
	
	public boolean has(char ch){
		boolean result;
		
		previousGuesses.add(ch); // store the new guess;
		
		if(letters[ch - 'a'] == true){
			letters[ch - 'a'] = false; 	// new letter guessed correctly
			lettersLeft--;				// one letter closer to the game end
			result = true;
		} else {
			result = false;
		}
		
		return result;
	}
	
	public boolean isRepeatedGuess(char ch){
		return this.previousGuesses.contains(ch);
	}
	
	public String stringSoFar(){
		sb.delete(0, sb.length()); // empty the string builder
		for(char ch : target){
			if(letters[ch - 'a'] == false) // if letters[ch - 'a'] contains true the letter has been correctly guessed
				sb.append(ch);
			else
				sb.append('_');
		}
		return sb.toString();
	}
	
	public String guessesSoFar(){
		return previousGuesses.toString();
	}
	
	public boolean isGameFinished(){
		return lettersLeft == 0;
	}
}
