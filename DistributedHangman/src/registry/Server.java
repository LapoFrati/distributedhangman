package registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.security.NoSuchAlgorithmException;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import messages.ConfigurationException;
import messages.JSONCodes;
import messages.ReadConfigurationFile;

public class Server {
	private static ReadConfigurationFile config;
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
		config = new ReadConfigurationFile("serverConfig.json");
    	String 	serverIP 				= config.getJsonField(JSONCodes.serverIP),
				registryName 			= config.getJsonField(JSONCodes.registryName),
				registryPort 			= config.getJsonField(JSONCodes.registryPort),
				baseMulticastAddr		= config.getJsonField(JSONCodes.baseMulticastAddr),
				maxMulticastAddr		= config.getJsonField(JSONCodes.maxMulticastAddr);
			int	maxNumberOfWaitingRooms = Integer.valueOf(config.getJsonField(JSONCodes.maxNumberOfGames)),
				timeoutLength			= Integer.valueOf(config.getJsonField(JSONCodes.timeoutLength));
    	
    	// Start registry for RMI
    	MyRegistry myLogin = new MyRegistry(maxNumberOfWaitingRooms,baseMulticastAddr,maxMulticastAddr);
        ServerSocket serverSocket = null;
        System.setProperty("java.rmi.server.hostname", serverIP);
        Registry reg = LocateRegistry.createRegistry(Integer.parseInt(registryPort));
        reg.rebind(registryName, myLogin);
        
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            throw new ConfigurationException("Could not listen on port: 4444.");
        }
        
        System.out.println("Server Listening");
        
        Executor myPool = Executors.newCachedThreadPool();
        try{
        	while(true){
        		myPool.execute(new ServerThread(serverSocket.accept(), timeoutLength));
        		}
        }finally{
        	serverSocket.close();
        }
    }
}
