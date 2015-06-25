package registryTest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.SecureRandom;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;

import registry.MulticastAddrGenerator;
import userAgent.TargetWord;

public class Test {
	
	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		SecureRandom random = new SecureRandom();
		BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in) );
		
		long baseMulticastAddr = ipToLong("224.0.0.0");
		System.out.println(baseMulticastAddr);
		
		long maxMulticastAddr = ipToLong("239.255.255.255");
		System.out.println(maxMulticastAddr);
		
		MulticastAddrGenerator testGenerator = new MulticastAddrGenerator("224.0.0.0", "224.0.0.4");
		System.out.println(maxMulticastAddr - baseMulticastAddr);
		for(int i = 0; i < 10; i++){
			System.out.println(testGenerator.getMulticastAddress());
		}
		
		System.out.println(nextPassword(random));
		System.out.println(nextPassword(random));
		System.out.println(nextPassword(random));
		System.out.println();
		System.out.println();
		
		String test = "arancia";
		char[] testArr = test.toCharArray();
		boolean[] lettersArr = new boolean[26];
		
		for(char ch : testArr){
			lettersArr[ch - 'a'] = true;
		}
		
		int counter = 0;
		char base = 'a';
		for(boolean bool : lettersArr){
			
			System.out.print(bool);
			System.out.println(" "+base);
			base = (char)((int)base+1);
		}
		
		System.out.println();
		System.out.println();
		
		TargetWord target = new TargetWord("arancia");
		System.out.println("Has a?");
		System.out.println(target.has('a'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has b?");
		System.out.println(target.has('b'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has c?");
		System.out.println(target.has('c'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has r?");
		System.out.println(target.has('r'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has n?");
		System.out.println(target.has('n'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has t?");
		System.out.println(target.has('t'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		System.out.println("Has i?");
		System.out.println(target.has('i'));
		System.out.println(target.stringSoFar());
		System.out.print("Game finished? ");
		System.out.println(target.isGameFinished());
		
		System.out.println(System.currentTimeMillis());
		
		BasicTextEncryptor textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword("asd");
		
		String myEncryptedText = textEncryptor.encrypt("prova");
		System.out.println(myEncryptedText);
		
		try{
			String plainText = textEncryptor.decrypt(myEncryptedText);
			System.out.println(plainText);
		}catch(EncryptionOperationNotPossibleException e){
			System.out.println("caught");
		}
		
		
		System.out.println(System.currentTimeMillis());
		
		//System.out.println("Received Guess = "+Test.getGuess());
		
	}
	
	public static class StaticTest{
		public static PrintingConstructor obj1 = new PrintingConstructor();
		public static Object obj;
		
		public StaticTest(){
			System.out.println("inside staticTest constructor");
			if(obj == null){
				System.out.println("building obj");
				obj = new Object();
			}else
				System.out.println("skip rebuilding obj");
		}
	}
	
	public static class PrintingConstructor{
		public PrintingConstructor(){
			System.out.println("inside printinc constructor");
		}
	}
	
		/* This works by choosing 130 bits from a cryptographically secure random bit generator, 
		 * and encoding them in base-32. 128 bits is considered to be cryptographically strong, but
		 *  each digit in a base 32 number can encode 5 bits, so 128 is rounded up to the next 
		 *  multiple of 5. This encoding is compact and efficient, with 5 random bits per character. 
		 */
		public static  String nextPassword(SecureRandom random) {
			return new BigInteger(130, random).toString(32);
		}
	
	public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                sb.insert(0, '.');
            }

            ip >>= 8;
        }

        return sb.toString();
    }
    
    public static char getGuess() throws IOException{
		String userInput;
		char result = 0;
		boolean guessAccepted = false;
		BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in) );
		
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
