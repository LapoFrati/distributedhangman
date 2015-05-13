package UserAgent;

import java.rmi.Remote;
import java.rmi.RemoteException;

public interface Login extends Remote {
	public boolean isRegisteredUser(String userName) throws RemoteException;
	public String getMulticastGroup(String userName) throws RemoteException;
}
