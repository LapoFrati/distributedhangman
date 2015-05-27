package registry;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.rmi.server.UnicastRemoteObject;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import org.jasypt.util.password.BasicPasswordEncryptor;

import userAgent.LoginIF;
import encryption.EncryptionUtil;
import encryption.PasswordGenerator;

public class MyRegistry extends UnicastRemoteObject implements LoginIF{
	
	private static final long 				serialVersionUID 	= -5884938279869629334L;
	private static BasicPasswordEncryptor 	passwordEncryptor 	= new BasicPasswordEncryptor();
	private static final String 			PUBLIC_KEY_FILE 	= "public.key", 
											PRIVATE_KEY_FILE 	= "private.key";
	
	public static 	PublicKey 				myPubKey;
	private static 	PrivateKey 				myPrivKey;
	//TODO use concurrent hashmap to cut the number of locks
	private static HashMap< String, UserInfo > 		users;
	private static LinkedList<UserNotificationIF> 	availableGuessers 		= new LinkedList<UserNotificationIF>();
	private static LinkedList< WaitingRoom >		waitingRoomsAvailable 	= new LinkedList<WaitingRoom>();
	private static MulticastAddrGenerator 			multicastAddrGenerator;
	
	private static final Object serializationLock 				= new Object(),
								currentNumberOfWaitingRoomsLock = new Object();
	
	private static int 			maxNumberOfWaitingRooms, 
								currentNumberOfWaitingRooms 	= 0;
	
	private static PasswordGenerator passwordGenerator 		= new PasswordGenerator();
	
	protected MyRegistry(int maxNumberOfWaitingRooms, String baseAddr, String maxAddr) throws FileNotFoundException, IOException, ClassNotFoundException {
		super();
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
	    
    	users = getRegisterdUserInfo();
    	MyRegistry.maxNumberOfWaitingRooms = maxNumberOfWaitingRooms;
    	multicastAddrGenerator = new MulticastAddrGenerator(baseAddr, maxAddr);
	    
	}

	/**
	 * Method called using RMI to check if the user is already registered
	 * 
	 * @param userName the name of the user whose status needs to be checked
	 * @return true if the user is a registered user
	 * @throws RemoteException
	 */
	public boolean isRegisteredUser(String userName) throws RemoteException {
		Boolean result;
		synchronized (users) {
			result = users.containsKey(userName);
		}
		return result;
	}

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
	public boolean logIn(String userName, byte[] text, Object callback) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		String password = EncryptionUtil.decrypt(text, myPrivKey); // The user's password is decrypted using the server's private key
		UserInfo userInfo;
		
		synchronized (users) {
			userInfo = users.get(userName); // Fetch the user's info	
			if(userInfo.isLoggedIn()){
				result = false; // the user is already logged in.
				((UserNotificationIF)callback).notifyUser("User already logged in!"); // Notify that the user is already logged in.
			}else{
				// Check if the passwords match using jasypt's utilities
				if ( passwordEncryptor.checkPassword(password, userInfo.getEncryptedPassword()) ) {
					userInfo.login();
					userInfo.setCallback((UserNotificationIF)callback); // Needed to remove it from the loggedUsers on logout
					System.out.println("User "+userName+" logged in");
					((UserNotificationIF)callback).notifyUser("Login successful!"); // Notify to the user login's completion
					notifySingleUserWaitingRooms((UserNotificationIF)callback);
					result = true;
				}
			}
		}
		
		return result;
	}

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
		
		synchronized (users) {
			if( !users.containsKey(userName) ){
				String password = EncryptionUtil.decrypt(text, myPrivKey); // The user's password is decrypted using the server's private key
				UserInfo newUserInfo = new UserInfo(passwordEncryptor.encryptPassword(password));
				users.put(userName, newUserInfo); // the user's password is digested using jasypt's utilities
				System.out.println("Registerd new user: "+userName);
				saveUsers(userName, newUserInfo.getEncryptedPassword());
				result = true;
			} else {
				System.out.println("Username already in use!");
			}
		}
		return result;
	}

	/**
	 * Method used to logout a user. Instead of having to provide the password, the user's host is checked against the host 
	 * used at login. This assumption is naive but is enough to simulate the required scenario.
	 *  
	 * @param userName the user name of the user requesting the logout
	 * @return true if the user has been correctly logged out, which means that
	 * @throws RemoteException
	 * @throws ServerNotActiveException
	 */
	public void logOut(String userName) throws RemoteException, ServerNotActiveException {
		UserInfo userInfo = null;
		UserNotificationIF callback = null;
		
		synchronized (users) {
			userInfo = users.get(userName);
			userInfo.logout();
			callback = userInfo.getCallback();
			System.out.println("User "+userName+" logged out.");
			(userInfo.getCallback()).closeCallback();
		}
		
		synchronized (availableGuessers) {
			availableGuessers.remove(callback);
		}
		
		// Handle master leaving and room closure.
		
		//UnicastRemoteObject.unexportObject(this, true);
	}

	public PublicKey getPublicKey() throws RemoteException {
		return myPubKey;
	}
	
	/**
	 * Method to notify the users the waitingRooms available
	 * 
	 * @throws RemoteException
	 */
	public static void notifyAllUsersWaitingRoomUpdate() throws RemoteException{
		
		StringBuilder sb = new StringBuilder("Rooms available: \n");
		synchronized (waitingRoomsAvailable) {
			for( WaitingRoom waitingRoom : waitingRoomsAvailable ){
				sb.append(waitingRoom.getRoomInfo()+"\n");
			}
		}
		
		String msg = sb.toString();
		synchronized (availableGuessers) {
			for( UserNotificationIF userCallback : availableGuessers){
				userCallback.notifyUser(msg);
			}
		}	
	}
	
	/**
	 * Method used to notify to a single user the waitingRooms available
	 * 
	 * @param callback the RMI-callback of the target user
	 * @throws RemoteException
	 */
	public static void notifySingleUserWaitingRooms(UserNotificationIF callback) throws RemoteException {
		StringBuilder sb = new StringBuilder("Rooms available: \n");
		synchronized (waitingRoomsAvailable) {
			for( WaitingRoom waitingRoom : waitingRoomsAvailable ){
				sb.append(waitingRoom.getRoomInfo()+"\n");
			}
		}
		callback.notifyUser(sb.toString());
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Method to save the registered users database to file using serialization
	 * 
	 * @param userName
	 * @param encryptedPassword
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public void saveUsers(String userName, String encryptedPassword) throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		synchronized (serializationLock) {
			ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
			retrievedInfo = (List<Pair>) input.readObject();
			input.close();
		
			retrievedInfo.add(new Pair(userName, encryptedPassword));
			
			// File with old name
		    File file = new File("users.data");
		    // File with new name
			Files.delete(Paths.get("users.data.backup")); 
		    File file2 = new File("users.data.backup");

		    // Rename file (or directory)
		    boolean success = file.renameTo(file2);
		    if (!success) {
		        // File was not successfully renamed
		    }
			
			ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("users.data"));
			
			output.writeObject(retrievedInfo);
			output.close();
		}
	}
	
	@SuppressWarnings("unchecked")
	/**
	 * Method to retrieve the registered users database from a serialized object
	 * @return
	 * @throws FileNotFoundException
	 * @throws IOException
	 * @throws ClassNotFoundException
	 */
	public HashMap<String,UserInfo> getRegisterdUserInfo() throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		HashMap<String, UserInfo> users = new HashMap<String, UserInfo>();
		
		ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
		retrievedInfo = (List<Pair>) input.readObject();
		input.close();
		System.out.println("Registered users: ");
		for( Pair pair : retrievedInfo){
			System.out.println(pair.getUserName());
			users.put(pair.getUserName(), new UserInfo(pair.getEncryptedPassword()));
		}
		
		return users;
	}
	
	public static boolean createNewWaitingRoom(String roomName, WaitingRoom newWaitingRoom){
		boolean result = false;
		
		// check if the number of rooms already created ;p
		synchronized (currentNumberOfWaitingRoomsLock) {
			if(currentNumberOfWaitingRooms < maxNumberOfWaitingRooms){
				currentNumberOfWaitingRooms++;
				newWaitingRoom.setPassword(passwordGenerator.nextPassword()); // the new room is still not in the list so we can access it without synchronization
				newWaitingRoom.setMulticast(multicastAddrGenerator.getMulticastAddress());
				result = true;
			}
			else{
				result = false;
			}
		} // the new room has not been added yet but the counter has been increased so we can release the lock
		
		if(result == true){
			
			synchronized (waitingRoomsAvailable) {
				waitingRoomsAvailable.add(newWaitingRoom);
			}
			
			synchronized (users) { // save room in the master info so that it can be retrieved by name
				users.get(roomName).setWaitingRoom(newWaitingRoom);
			}
			
			try {
				notifyAllUsersWaitingRoomUpdate();
			} catch (RemoteException e) {
				e.printStackTrace();
			}
		}
		
		return result;
	}
	
	public static void addAvailableGuesser(String userName){
		UserNotificationIF callback;
		synchronized(users){
			callback = users.get(userName).getCallback();
		}
		
		synchronized (availableGuessers) {
			availableGuessers.add(callback);
		}
	}
	
	public static void removeAvailableGuesser(String userName){
		UserNotificationIF callback;
		
		synchronized(users){
			callback = users.get(userName).getCallback();
		}
		
		synchronized (availableGuessers) {
			availableGuessers.remove(callback);
		}
	}
	
	public static boolean joinRoom(String roomName){
		boolean result;
		WaitingRoom room;
		synchronized (users) {
			room = users.get(roomName).getWaitingRoom();
			result = (room != null ) ? room.addGuesser() : false;
		}
		return result;
	}
	
	public static void leaveRoom(String roomName){
		synchronized(users){
			users.get(roomName).getWaitingRoom().removeGuesser();
		}
	}
	
	public static WaitingRoomLock getRoomWaitLock(String roomName){
		WaitingRoomLock lock;
		synchronized(users){
			lock = users.get(roomName).getWaitingRoom().getWaitLock();
		}
		return lock;
	}
	
	public static void closeWaitingRoom(String user, WaitingRoom room){
		
		synchronized (users) { // set the room to null to stop new users from joining it
			users.get(user).setWaitingRoom(null);
		}
		synchronized (waitingRoomsAvailable) {
			waitingRoomsAvailable.remove(room);
		}
	}
	
}
