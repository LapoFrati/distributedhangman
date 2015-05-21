package registryTest;

import registry.MulticastAddrGenerator;

public class Test {
	
	public static void main(String[] args) {
		long baseMulticastAddr = ipToLong("224.0.0.0");
		System.out.println(baseMulticastAddr);
		
		long maxMulticastAddr = ipToLong("239.255.255.255");
		System.out.println(maxMulticastAddr);
		
		MulticastAddrGenerator testGenerator = new MulticastAddrGenerator("224.0.0.0", "224.0.0.4");
		
		for(int i = 0; i < 10; i++){
			System.out.println(testGenerator.getMulticastAddress());
		}
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
