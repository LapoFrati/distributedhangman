package registry;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import org.jasypt.util.password.BasicPasswordEncryptor;
import org.jasypt.util.text.BasicTextEncryptor;

import userAgent.LoginIF;
import userAgent.UserConfiguration;
import encryption.EncryptionUtil;

public class Server extends UnicastRemoteObject implements LoginIF {

	private static final long serialVersionUID = -350410173384548551L;
	private static BasicPasswordEncryptor passwordEncryptor;
	private static BasicTextEncryptor textEncryptor;
	private static final String PUBLIC_KEY_FILE = "public.key", PRIVATE_KEY_FILE = "private.key";
	private static PublicKey myPubKey;
	private static PrivateKey myPrivKey;
	private static Map< String, UserInfo > users;
	private static List< InetAddress > multicastAddresses;
	private static List< UserNotificationIF > loggedUsers = Collections.synchronizedList(new LinkedList<UserNotificationIF>());
	private static List< WaitingRoom > gamesAvailable = Collections.synchronizedList(new LinkedList<WaitingRoom>());
	private static int maxNumberOfGames;
	private static ServerConfiguration config;
	
	protected Server() throws FileNotFoundException, IOException {
		super();
		passwordEncryptor = new BasicPasswordEncryptor();
		textEncryptor = new BasicTextEncryptor();
		if (!EncryptionUtil.areKeysPresent())
			
			try {
				EncryptionUtil.generateKey();
			} catch (NoSuchAlgorithmException | IOException e) {
				System.out.println("Could not generate public and private keys.");
				e.printStackTrace();
			}      
			
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
	    try {
			myPubKey = (PublicKey) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Could not read the public key.");
			e.printStackTrace();
		}
	    inputStream.close();
	    
	    inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
	    try {
			myPrivKey = (PrivateKey) inputStream.readObject();
		} catch (ClassNotFoundException e) {
			System.out.println("Could not read the private key");
			e.printStackTrace();
		}
	    inputStream.close();	
	}
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
    	config = new ServerConfiguration("serverConfig.json");
    	String 	serverIP = config.getServerIp(), 
				serverName = config.getRegistryName(),
				serverPort = config.getRegistryPort();
    	users = config.getRegisterdUserInfo();
    	//multicastAddresses = config.getMulticastAddresses(); NOT WORKING
    	maxNumberOfGames = config.getMaxNumberOfGames();
    	
    	Server myLogin = new Server();
        ServerSocket serverSocket = null;
    	
        System.setProperty("java.rmi.server.hostname", serverIP);
        Registry reg = LocateRegistry.createRegistry(Integer.parseInt(serverPort));
        reg.rebind(serverName, myLogin);
        
        Executor myPool = Executors.newCachedThreadPool();
        
        try {
            serverSocket = new ServerSocket(4444);
        } catch (IOException e) {
            System.err.println("Could not listen on port: 4444.");
            System.exit(-1);
        }
        
        System.out.println("Server Listening");
        
        try{
        	while(true){
        		myPool.execute(new ServerThread(serverSocket.accept(), 10));
        		// TODO: read the timeout length from the config file
        		}
        }finally{
        	serverSocket.close();
        }
    }

	@Override
	/**
	 * Method called using RMI to check if the user is already registered
	 * 
	 * @param userName the name of the user whose status needs to be checked
	 * @return true if the user is a registered user
	 * @throws RemoteException
	 */
	public synchronized boolean isRegisteredUser(String userName) throws RemoteException {
		return users.containsKey(userName);
	}

	@Override
	/**
	 * Method used to login.
	 * 
	 * @param userName the user that wants to login
	 * @param text the password chosen by the user, encrypted with server's public key
	 * @param callback the object that will be used for the RMI-callback 
	 * @return true if the login is successful, false if the passwords don't match
	 * @throws RemoteException
	 * @throws ServerNotActiveException
	 */
	public synchronized boolean logIn(String userName, byte[] text, Object callback) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		String password = EncryptionUtil.decrypt(text, myPrivKey); // The user's password is decrypted using the server's private key
		UserInfo userInfo = users.get(userName); // Fetch the user's info
		
		// Check if the passwords match using jasypt's utilities
		if ( passwordEncryptor.checkPassword(password, userInfo.getEncryptedPassword()) ) {
			userInfo.login();
			userInfo.setHost(getClientHost());
			userInfo.setCallback((UserNotificationIF)callback); // Needed to remove it from the loggedUsers on logout
			loggedUsers.add((UserNotificationIF) callback); // Needed for the RMI-callbacks that will notify the available rooms to the user
			System.out.println("User "+userName+" logged in from: "+users.get(userName).getHost());
			((UserNotificationIF)callback).notifyUser("Login successful!"); // Notify to the user login's completion
			notifySingleUserWaitingRooms(userInfo.getCallback());
			result = true;
		}
		return result;
	}

	@Override
	/**
	 * Method used to register a new user. 
	 * 
	 * PostCondition: when the method returns the userName is registered.
	 * 
	 * @param userName the user name of the user that wants to register
	 * @param text the password chosen by the user, encrypted with server's public key
	 * @return true if the registration is successful, false if the userName is already registered. 
	 * @throws RemoteException
	 * @throws ServerNotActiveException
	 */
	public synchronized boolean registerNewUser(String userName, byte[] text) throws ServerNotActiveException, FileNotFoundException, ClassNotFoundException, IOException {
		boolean result = false;
		if( !users.containsKey(userName) ){
			String password = EncryptionUtil.decrypt(text, myPrivKey); // The user's password is decrypted using the server's private key
			UserInfo newUserInfo = new UserInfo(passwordEncryptor.encryptPassword(password));
			newUserInfo.setHost(getClientHost());
			users.put(userName, newUserInfo); // the user's password is digested using jasypt's utilities
			System.out.println("Registerd new user: "+userName);
			config.saveUsers(userName, newUserInfo.getEncryptedPassword());
			result = true;
		} else {
			System.out.println("Username already in use!");
		}
		return result;
	}

	@Override
	/**
	 * Method used to logout a user. Instead of having to provide the password, the user's host is checked against the host 
	 * used at login. This assumption is naive but is enough to simulate the required scenario.
	 *  
	 * @param userName the user name of the user requesting the logout
	 * @return true if the user has been correctly logged out, which means that
	 * @throws RemoteException
	 * @throws ServerNotActiveException
	 */
	public synchronized boolean logOut(String userName) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		UserInfo userInfo = null;
		
		if(users.containsKey(userName)){
			userInfo = users.get(userName);
			if(userInfo.isLoggedIn() && (getClientHost()).equals(userInfo.getHost())){
				users.get(userName).logout();
				loggedUsers.remove(userInfo.getCallback());
				System.out.println("User "+userName+" logged out.");
				//Naming.unbind("name");
				//UnicastRemoteObject.unexportObject(this, true);
				result = true;
			}
		}
		return result;
	}

	@Override
	public synchronized PublicKey getPublicKey() throws RemoteException {
		return myPubKey;
	}
	
	/**
	 * Method to notify the users the waitingRooms available
	 * 
	 * @throws RemoteException
	 */
	public static synchronized void notifyAllUsersWaitingRoomUpdate() throws RemoteException{
		for( UserNotificationIF userCallback : loggedUsers){
			userCallback.notifyUser("Rooms available: ");
			for( WaitingRoom waitingRoom : gamesAvailable ){
				userCallback.notifyUser(waitingRoom.getRoomInfo());
			}
		}
	}
	
	/**
	 * Method used to notify to a single user the waitingRooms available
	 * 
	 * @param callback the RMI-callback of the target user
	 * @throws RemoteException
	 */
	public static synchronized void notifySingleUserWaitingRooms(UserNotificationIF callback) throws RemoteException {
		callback.notifyUser("Rooms available: ");
		for( WaitingRoom waitingRoom : gamesAvailable ){
			callback.notifyUser(waitingRoom.getRoomInfo());
		}
	}
}
