package Registry;

import java.math.BigInteger;
import java.security.SecureRandom;

public class PasswordGenerator {
	private static SecureRandom random = new SecureRandom();
	
	/* This works by choosing 130 bits from a cryptographically secure random bit generator, 
	 * and encoding them in base-32. 128 bits is considered to be cryptographically strong, but
	 *  each digit in a base 32 number can encode 5 bits, so 128 is rounded up to the next 
	 *  multiple of 5. This encoding is compact and efficient, with 5 random bits per character. 
	 */
	public String nextPassword() {
		return new BigInteger(130, random).toString(32);
	}
}

