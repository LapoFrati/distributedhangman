package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import encryption.EncryptionUtil;
import messages.JSONCodes;
import messages.ReadConfigurationFile;

public class UserLogin {

	private static String userName;
	
	protected UserLogin() throws RemoteException {
		super();
	}
	public static void main(String[] args) throws NotBoundException, IOException, ClassNotFoundException, ServerNotActiveException {
		
		ReadConfigurationFile config = new ReadConfigurationFile("userConfig.json");
		String 	serverIP     	= config.getJsonField(JSONCodes.serverIP), 
				registryName 	= config.getJsonField(JSONCodes.registryName),
				registryPort 	= config.getJsonField(JSONCodes.registryPort),
				masterServerPort   	= config.getJsonField(JSONCodes.masterServerPort),
				guesserServerPort 	= config.getJsonField(JSONCodes.guesserServerPort),
				password		= "",
				role			= "";
		boolean loginError 	= false;
		byte[] encryptedPassword = null;
		
		BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in) );
		
		// Use RMI to try to login
		LoginIF reg = (LoginIF) Naming.lookup("rmi://"+serverIP+":"+registryPort+"/"+registryName);
		
		do{	// First try:  loginError = false -> requests user name
			// Second try: loginError = true  -> prints message instead of asking for the user name ( 	if userName exists -> only the password could be wrong
			//																							if userName does not exist ->  password will be accepted )
			if(loginError){ 
				System.out.println("Wrong password!");
			}else{
				do{ 
					System.out.println("User Name: ");
				}while((userName = stdIn.readLine()) == null);
			}
			
			do{ 
				System.out.println("Password: ");
				loginError = true; // If the registry's login fails the next iteration will begin with loginError = true
			}while((password = stdIn.readLine()) == null);
			
			try { // Encrypts the password using the registry's public key ( the public key's retrieval's security could be improved )
				encryptedPassword =  EncryptionUtil.encrypt(password, reg.getPublicKey());
			} catch (InvalidKeyException | NoSuchAlgorithmException
					| NoSuchPaddingException | IllegalBlockSizeException
					| BadPaddingException | RemoteException e) {
				System.out.println("Failed to encrypt password.");
				e.printStackTrace();
			}
			
			// If the userName is not already registered, do so.
			if(reg.isRegisteredUser(userName) == false){
				System.out.println("Registering new user: "+userName);
				reg.registerNewUser(userName, encryptedPassword);
			}
			
		}while(reg.logIn(userName, encryptedPassword, new UserNotificationCallback()) == false);
		
		// Here the user is logged in
		
		do{
    		System.out.println("Choose role: [m or g]");
    		role = stdIn.readLine();
    	}while(!role.matches("m|g"));
		
		switch(role){
			case "g":	new GuesserLauncher(stdIn, userName, serverIP, guesserServerPort).startGuesser();
						break;
			case "m": 	new MasterLauncher(stdIn, userName, serverIP, masterServerPort).startMaster();
						break;
		}
		reg.logOut(userName);
		System.out.println(userName + " logged out.");
	}
}
