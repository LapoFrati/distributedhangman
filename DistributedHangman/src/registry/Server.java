package registry;

import java.io.IOException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import messages.JSONCodes;
import messages.ReadConfigurationFile;

public class Server {
	private static ReadConfigurationFile config;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
		config = new ReadConfigurationFile("serverConfig.json");
    	
		String 	serverIP 				= config.getJsonField(JSONCodes.serverIP),
				registryName 			= config.getJsonField(JSONCodes.registryName),
				registryPort 			= config.getJsonField(JSONCodes.registryPort),
				masterServerPort   		= config.getJsonField(JSONCodes.masterServerPort),
				guesserServerPort 		= config.getJsonField(JSONCodes.guesserServerPort),
				baseMulticastAddr		= config.getJsonField(JSONCodes.baseMulticastAddr),
				maxMulticastAddr		= config.getJsonField(JSONCodes.maxMulticastAddr);
		
    	int	maxNumberOfWaitingRooms = Integer.valueOf(config.getJsonField(JSONCodes.maxNumberOfGames)),
			timeoutLength			= Integer.valueOf(config.getJsonField(JSONCodes.timeoutLength));
			
		MasterConnectionListener masterConnectionListener 		= new MasterConnectionListener(masterServerPort, timeoutLength);
	    GuesserConnectionListener guesserConnectcionListener 	= new GuesserConnectionListener(guesserServerPort, timeoutLength);

    	
    	// Start registry for RMI
    	MyRegistry myLogin = new MyRegistry(maxNumberOfWaitingRooms,baseMulticastAddr,maxMulticastAddr);
        System.setProperty("java.rmi.server.hostname", serverIP);
        Registry reg = LocateRegistry.createRegistry(Integer.parseInt(registryPort));
        reg.rebind(registryName, myLogin);
        
        masterConnectionListener.start();
        guesserConnectcionListener.start();
        
        System.out.println("Server Listening");
    }
}
