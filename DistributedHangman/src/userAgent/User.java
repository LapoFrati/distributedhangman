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

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import messages.JSONCodes;
import messages.ReadConfigurationFile;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

import registry.UserNotificationIF;
import encryption.EncryptionUtil;

public class User extends UnicastRemoteObject implements UserNotificationIF{
	
	private static final long serialVersionUID = 1L;
	private static String userName;

	protected User() throws RemoteException {}

	@SuppressWarnings("unchecked")
	public static void main(String[] args) throws IOException, NotBoundException, ClassNotFoundException{

		ReadConfigurationFile config = new ReadConfigurationFile("userConfig.json");
		String 	serverIP     	= config.getJsonField(JSONCodes.serverIP), 
				registryName 	= config.getJsonField(JSONCodes.registryName),
				registryPort 	= config.getJsonField(JSONCodes.registryPort),
				serverPort   	= config.getJsonField(JSONCodes.serverPort),
				password		= "",
				multicast		= "",
				role			= "";
		
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

	        	do{
	        		System.out.println("Choose role: ");
	        		role = stdIn.readLine();
	        	}while(!role.matches("m|g"));
	        	
	        	JSONObject messageToServer, messageFromServer = null;
	        	
	        	switch(role){
	        	case "m": {	messageToServer = new JSONObject();
				        	int 	numberOfGuessers = 4,
				        			numberOfAttempts = 0;
			        		
				        	boolean readNumber = true,
			        				keepReading = true,
			        				attemptsOk = false,
			        				proceed = false;
				        	
				        	String 	targetword = "",
		        					reply = "";
				        	
			        		System.out.println("Required number of guesser: ");
			        		while(readNumber){
			        			try{
			        			numberOfGuessers = Integer.valueOf(stdIn.readLine());
			        			readNumber = false;
			        			}catch (NumberFormatException e) {
									System.out.println("Wrong number format.");
								}
			        		}
	        				messageToServer.put(JSONCodes.role, JSONCodes.master);
			        		messageToServer.put(JSONCodes.roomName, userName);
			        		messageToServer.put(JSONCodes.numberOfGuessers, numberOfGuessers);
			        		
			        		System.out.println("Sending info");
			        		out.println(messageToServer);
			        		System.out.println("Waiting for reply");
			        		while(keepReading){
				        		try {
									messageFromServer = (JSONObject) new JSONParser().parse(in.readLine());
									
								} catch (ParseException e) {
									e.printStackTrace();
								}
			        			switch((String) messageFromServer.get(JSONCodes.message)){
			        				case JSONCodes.waitingRoomsFull: 	System.out.println("Waiting to create a new room");
			        													break;
			        				case JSONCodes.newRoomCreated:		System.out.println("New room created. Waiting for guessers.");
			        													break;
			        				case JSONCodes.gameStarting:		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
					        											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
					        											break;	
			        				case JSONCodes.connectionClosed: 	keepReading = false;
			        													System.out.println("Connection closed.");
			        													break;
			        			}
			        		}
			        		
			        		System.out.println(password + "-" + multicast);
			        		
		        			System.out.println("Starting Master");
		        			
		        			System.out.println("Choose the target word");
		        			
		        			do{
		        				targetword = stdIn.readLine();
		        				System.out.println("The chosen word is: " + targetword+".");
		        				do{
		        					System.out.println("Proceed? [Y/N]");
		        					reply = stdIn.readLine(); 
		        				}while(reply.matches("y|Y|n|N"));
		        				
		        				if(reply.matches("n|N")){
		        					proceed = false;
		        				} else {
		        					proceed = true;
		        				}
		        			}
		        			while(!proceed);
		        			
		        			System.out.println("Choose number of allowed attempts.");
		        			while(!attemptsOk){
		        				try {
		        					numberOfAttempts = Integer.valueOf(stdIn.readLine());
								} catch (Exception e) {}
		        				if(numberOfAttempts >0 ){
		        					attemptsOk = true;
		        				} else {
		        					attemptsOk = false;
		        				}
		        			}
		        			
		        			MasterWorker master = new MasterWorker(password, multicast, targetword, numberOfAttempts,numberOfGuessers);
		        			master.startGame();
			        		break;
	        			  }
	        	case "g":{	Boolean keepReading = true;
	        				messageToServer = new JSONObject();
			        		messageToServer.put(JSONCodes.role, JSONCodes.guesser);
			        		messageToServer.put(JSONCodes.userName, userName);
			        		out.println(messageToServer);
			        		
			        		System.out.println("Choose room to join: ");
			        		messageToServer.put(JSONCodes.roomName, stdIn.readLine());
			        		out.println(messageToServer);
			        		while(keepReading){
			        			try {
			        				messageFromServer = (JSONObject) new JSONParser().parse(in.readLine());
									
								} catch (ParseException e) {
									System.out.println("Message:" + messageFromServer);
									e.printStackTrace();
								}
			        			switch((String) messageFromServer.get(JSONCodes.message)){
			        			
			        				case JSONCodes.roomJoined: 			System.out.println("Joined selected room, please wait for game to start");
			        													break;
			        				case JSONCodes.connectionClosed:	System.out.println("Game is starting");
			        													keepReading = false;
			        													break;
			        				case JSONCodes.gameStarting: 		password  = (String) messageFromServer.get(JSONCodes.roomPassword);
					        											multicast = (String) messageFromServer.get(JSONCodes.roomMulticast);
					        											break;
			        				case JSONCodes.roomClosed:			System.out.println("Room closed.");
			        				case JSONCodes.guesserJoinError:	System.out.println("Choose room to join: ");
					        											out.println(stdIn.readLine());
					        											break;
			        			}		
			        		}
			        		System.out.println(password + "-" + multicast);
			        		GuesserReceiver guesser = new GuesserReceiver(userName, password,multicast);
			        		guesser.startGame();
	        				break;
	        			 }
	        	}
	        	
	        	try {
	        		System.out.println("Logging out "+userName);
	     			reg.logOut(userName);
	     		} catch (ServerNotActiveException e) {
	     			e.printStackTrace();
	     		}
        
            } catch (UnknownHostException e) {
                System.out.println("Don't know about host " + InetAddress.getByName(serverIP));
            } catch (IOException e) {
                System.out.println("Couldn't get I/O for the connection to " +
                		InetAddress.getByName(serverIP));
            }
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
	public void notifyUser(String msg) throws RemoteException {
		System.out.println(msg);
	}

	@Override
	public void closeCallback() throws RemoteException {
		//Naming.unbind("name");
		UnicastRemoteObject.unexportObject(this, true);
	}
}
