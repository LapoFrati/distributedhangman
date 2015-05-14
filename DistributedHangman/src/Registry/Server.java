package Registry;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
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
import java.util.Map;

import org.jasypt.util.password.BasicPasswordEncryptor;

import Encryption.EncryptionUtil;
import UserAgent.Login;
import UserAgent.RegistryConfiguration;

public class Server extends UnicastRemoteObject implements Login {

	private static final long serialVersionUID = -350410173384548551L;
	private static BasicPasswordEncryptor passwordEncryptor;
	private static Map<String, UserInfo> users = Collections.synchronizedMap(new HashMap<String, UserInfo>());
	private static final String PUBLIC_KEY_FILE = "public.key";
	private static final String PRIVATE_KEY_FILE = "private.key";
	private static PublicKey myPubKey;
	private static PrivateKey myPrivKey;
	
	protected Server() throws NoSuchAlgorithmException, IOException, ClassNotFoundException {
		super();
		passwordEncryptor = new BasicPasswordEncryptor();
		
		if (!EncryptionUtil.areKeysPresent())
		       EncryptionUtil.generateKey();      
			
		ObjectInputStream inputStream = new ObjectInputStream(new FileInputStream(PUBLIC_KEY_FILE));
	    myPubKey = (PublicKey) inputStream.readObject();
	    inputStream.close();
	    
	    inputStream = new ObjectInputStream(new FileInputStream(PRIVATE_KEY_FILE));
	    myPrivKey = (PrivateKey) inputStream.readObject();
	    inputStream.close();
			
	}
	
	public static void main(String[] args) throws IOException, NoSuchAlgorithmException, ClassNotFoundException {
    	RegistryConfiguration config = new RegistryConfiguration("config.json");
    	String 	serverIP = config.getServerIp(), 
				serverName = config.getServerName(),
				serverPort = config.getServerPort();
    	
        System.setProperty("java.rmi.server.hostname", serverIP);
        Server myLogin = new Server();
        Registry reg = LocateRegistry.createRegistry(Integer.parseInt(serverPort));
        reg.rebind(serverName, myLogin);
        System.out.println("Server Ready");
    }

	@Override
	public synchronized boolean isRegisteredUser(String userName) throws RemoteException {
		return users.containsKey(userName);
	}

	@Override
	public synchronized boolean logIn(String userName, byte[] text) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		String password = EncryptionUtil.decrypt(text, myPrivKey);
		if ( users.get(userName).isLoggedIn() == false 
			 && passwordEncryptor.checkPassword(password, users.get(userName).getEncryptedPassword())) {
			users.get(userName).login();
			users.get(userName).setHost(getClientHost());
			System.out.println("User "+userName+" logged in from: "+users.get(userName).getHost());
			result = true;
		}
		return result;
	}

	@Override
	public synchronized boolean registerNewUser(String userName, byte[] text) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		String password = EncryptionUtil.decrypt(text, myPrivKey);
		if( !users.containsKey(userName) ){
				users.put(userName, new UserInfo(passwordEncryptor.encryptPassword(password), getClientHost()));
				System.out.println("Registerd new user: "+userName);
				result = true;
		} else {
			System.out.println("Username already in use!");
		}
		return result;
	}

	@Override
	public boolean logOut(String userName) throws RemoteException, ServerNotActiveException {
		boolean result = false;
		
		if(getClientHost().equals(users.get(userName).getHost())){
			
		}
		return result;
	}

	@Override
	public PublicKey getPublicKey() throws RemoteException {
		return myPubKey;
	}

}
