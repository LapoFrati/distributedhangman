package registry;

import messages.ConfigurationException;

public class MulticastAddrGenerator {
	static long baseAddr, maxAddr, range, counter;
	
	public MulticastAddrGenerator(String newBaseAddr, String newMaxAddr) {
		baseAddr = ipToLong(newBaseAddr);
		maxAddr  = ipToLong(newMaxAddr);
		counter = 0;
		range = maxAddr - baseAddr;
		
		if(	range < 0 )
			throw new ConfigurationException("Base multicast address is bigger than max multicast address.");
		
		if(	baseAddr < Long.valueOf("3758096384") /* 224.0.0.0 */ 
				|| baseAddr  > Long.valueOf("4026531839") /* 239.255.255.255 */
				|| maxAddr < Long.valueOf("3758096384") 
				|| maxAddr  > Long.valueOf("4026531839"))
			throw new ConfigurationException("Multicast addresses out of range.");
		
	}
	
	public static String getMulticastAddress(){
		String generatedIP = longToIp(baseAddr + counter);
			counter = (counter + 1)%(range+1);
		return generatedIP;
	}
	
	private long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    private static String longToIp(long ip) {
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
