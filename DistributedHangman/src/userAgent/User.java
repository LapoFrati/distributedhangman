package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import messages.JSONCodes;
import messages.TCPmsg;

import org.json.simple.JSONObject;

import registry.UserNotificationIF;
import encryption.EncryptionUtil;

public class User extends UnicastRemoteObject implements UserNotificationIF{
	
	private static final long serialVersionUID = 1L;
	private static String userName;

	protected User() throws RemoteException {}

	public static void main(String[] args) throws IOException, NotBoundException, ClassNotFoundException{

		Scanner scanner = new Scanner(System.in);
		UserConfiguration config = new UserConfiguration("userConfig.json");
		String 	serverIP     = config.getServerIp(), 
				registryName = config.getRegistryName(),
				registryPort = config.getRegistryPort(),
				serverPort   = config.getServerPort();
		boolean logout = false;
		BufferedReader stdIn = new BufferedReader( new InputStreamReader(System.in) );
		// Use RMI to try to login
		LoginIF reg = (LoginIF) Naming.lookup("rmi://"+serverIP+":"+registryPort+"/"+registryName);
		
		try {
			userLogin(reg, stdIn);
		} catch (ServerNotActiveException e1) {
			e1.printStackTrace();
		}
		
		// Now the user is logged in
        
        try (	// Create a socket to connect to the server using the info in the configuration file
                Socket serverSocket = new Socket(InetAddress.getByName(serverIP), Integer.parseInt(serverPort));
                
        		// TCP socket provides bidirectional I/O. Attach in/out to the respective streams.
        		PrintWriter out =
                    new PrintWriter(serverSocket.getOutputStream(), true);
                BufferedReader in =
                    new BufferedReader(
                        new InputStreamReader(serverSocket.getInputStream()));
            ){
        		
	        	String role,messageFromServer;
	        	do{
	        		System.out.println("Choose role: ");
	        		role = scanner.nextLine();
	        	}while(!role.matches("m|g"));
	        	
	        	JSONObject messageToServer;
	        	
	        	switch(role){
	        	case "m": 	messageToServer = new JSONObject();
	        				//TODO: read fields from input
	        				messageToServer.put(JSONCodes.role, JSONCodes.master);
			        		messageToServer.put(JSONCodes.roomName, userName);
			        		messageToServer.put(JSONCodes.numberOfGuessers, 10);
			        		System.out.println("Sending info");
			        		out.println(messageToServer);
			        		System.out.println("Waiting for reply");
			        		
			        		while(!(messageFromServer = in.readLine()).equals(TCPmsg.connectionClosed)){
			        			switch(messageFromServer){
			        				case TCPmsg.ack: 	System.out.println("ACK received");
			        									break; 
			        			}
			        		};
			        		
			        		System.out.println(TCPmsg.connectionClosed+" received");
			        		
			        		//Master master = new Master(out, in, userName);
			        		//master.createGame();
			        		break;
			        		
	        	case "g" :	//TODO: implement guesser protocol
	        				/*
	        				messageToServer = new JSONObject();
			        		messageToServer.put("ACTION", "BECOME_GUESSER");
			        		
			        		out.println(messageToServer);
			        		
			        		Guesser guesser = new Guesser(out, in, userName);
			        		guesser.searchForGame();
			        		*/
	        				break;
	        	}
        
        	
        /*	
        	try {
        		 System.out.println("Logging out "+userName);
     			reg.logOut(userName);
     		} catch (ServerNotActiveException e) {
     			// TODO Auto-generated catch block
     			e.printStackTrace();
     		}
        */	
            } catch (UnknownHostException e) {
                System.err.println("Don't know about host " + InetAddress.getByName(serverIP));
                System.exit(1);
            } catch (IOException e) {
                System.err.println("Couldn't get I/O for the connection to " +
                		InetAddress.getByName(serverIP));
                System.exit(1);
            }
           
		scanner.close();
	}
	
	/**
	 * Method that takes care of the user's login. Asks for user name and password, creates a new user if needed, and then 
	 * proceeds with the login. If the login fails it asks again for the password until it succeed or is terminated.
	 * 
	 * PostCondition: when the method returns the user is logged in.
	 * 
	 * @param reg the registry that will be used with RMI
	 * @param stdIn the BufferedReader used to get the user input
	 * @throws ServerNotActiveException
	 * @throws IOException 
	 * @throws ClassNotFoundException 
	 */
	private static void userLogin(LoginIF reg, BufferedReader stdIn) throws ServerNotActiveException, IOException, ClassNotFoundException {
		String password = "uninitializedPassword";
		boolean loginError = false;
		byte[] encryptedPassword = null;
		
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
			
			try { // Encrypts the password using the registry's public key ( the public key's retrieval's security could be improved but is deemed enough for now)
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
		
		}while(reg.logIn(userName, encryptedPassword, new User()) == false);
	}
	
	/**
	 * This method implements the RMI-callback from the registry to the client used to print messages (the list of available rooms)
	 * 
	 * @param msg message to be displayed
	 * @throws RemoteException
	 */
	@Override
	public void notifyUser(String msg) throws RemoteException {
		System.out.println(msg);
	}
}
