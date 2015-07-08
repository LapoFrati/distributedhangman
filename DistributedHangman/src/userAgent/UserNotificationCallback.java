package userAgent;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import registry.UserNotificationIF;

public class UserNotificationCallback extends UnicastRemoteObject implements UserNotificationIF{

	private static final long serialVersionUID = -6197768880301021351L;

	protected UserNotificationCallback() throws RemoteException {
		super();
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

	/**
	 * This method is used to close the callback once is not needed anymore.
	 * 
	 * @throws RemoteException
	 */
	public void closeCallback() throws RemoteException {
		//Naming.unbind("name");
		UnicastRemoteObject.unexportObject(this, true);
	}
}
