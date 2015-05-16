package UserAgent;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.security.PublicKey;

public interface LoginIF extends Remote {
	public boolean isRegisteredUser(String userName) throws RemoteException;
	public boolean logIn(String userName, byte[] publicKeyEncryptedPassword, Object callback) throws RemoteException, ServerNotActiveException;
	public boolean registerNewUser(String userName, byte[] publicKeyEncryptedPassword) throws RemoteException, ServerNotActiveException;
	public boolean logOut(String userName) throws RemoteException, ServerNotActiveException;
	public PublicKey getPublicKey() throws RemoteException;
}
