package registryTest;

import java.math.BigInteger;
import java.security.SecureRandom;

import registry.MulticastAddrGenerator;

public class Test {
	
	public static void main(String[] args) {
		SecureRandom random = new SecureRandom();
		
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

}
