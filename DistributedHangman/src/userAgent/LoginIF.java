package userAgent;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.rmi.Remote;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.security.PublicKey;

public interface LoginIF extends Remote {
	public boolean isRegisteredUser(String userName) throws RemoteException;
	public boolean logIn(String userName, byte[] publicKeyEncryptedPassword, Object callback) throws RemoteException, ServerNotActiveException;
	public boolean registerNewUser(String userName, byte[] publicKeyEncryptedPassword) throws RemoteException, ServerNotActiveException, FileNotFoundException, ClassNotFoundException, IOException;
	public void logOut(String userName) throws RemoteException, ServerNotActiveException;
	public PublicKey getPublicKey() throws RemoteException;
}
