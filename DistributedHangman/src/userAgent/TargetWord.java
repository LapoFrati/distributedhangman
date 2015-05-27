package userAgent;

public class TargetWord {
	char[] target;
	boolean[] letters;
	int lettersLeft;
	StringBuilder sb;
	
	public TargetWord(String target){
		lettersLeft = 0;
		this.target = target.toCharArray();
		
		/* Array accessed by the char value. Contains true if the letters in in the word 
		 * and has not been guessed yet.
		 */
		letters = new boolean[26]; 
		
		for(char ch : this.target){
			if(letters[ch - 'a'] == false){
				letters[ch - 'a'] = true;
				lettersLeft++; // counts how many different letters there are, used to determine when the word has been guessed
			}
		}
		sb = new StringBuilder();
	}
	
	public boolean has(char ch){
		boolean result;
		
		if(letters[ch - 'a'] == true){
			letters[ch - 'a'] = false; // new letter guessed correctly
			lettersLeft--;
			result = true;
		} else {
			result = false;
		}
		
		return result;
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
	
	public boolean isGameFinished(){
		return lettersLeft == 0;
	}
	
	public void test(){
		String test = "arancia";
		char[] testArr = test.toCharArray();
		boolean[] lettersArr = new boolean[26];
		
		for(char ch : testArr){
			lettersArr[ch - 'a'] = true;
		}
		
		char base = 'a';
		for(boolean bool : lettersArr){
			
			System.out.print(bool);
			System.out.println(" "+base);
			base = (char)((int)base+1);
		}
	}
}
