package UserAgent;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;

public class User{
	public static void main(String[] args) throws IOException, NotBoundException {
		if(args.length != 1 )
			throw new IllegalArgumentException("Wrong argument.");
			
		RegistryConfiguration config = new RegistryConfiguration("config.json");
		String 	serverIP = config.getServerIp(), 
				serverName = config.getServerName(),
				serverPort = config.getServerPort();
		
		Login reg = (Login) Naming.lookup("rmi://"+serverIP+":"+serverPort+"/"+serverName);
		
		if(reg.isRegisteredUser(args[0])){
			
		}else{
			
		}
			
	}
}
