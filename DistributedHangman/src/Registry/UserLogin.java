package Registry;

import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import UserAgent.Login;

public class UserLogin extends UnicastRemoteObject implements Login {

	private static final long serialVersionUID = -350410173384548551L;

	protected UserLogin() throws RemoteException {
		super();
		// TODO Auto-generated constructor stub
	}

	@Override
	public boolean isRegisteredUser(String userName) throws RemoteException {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public String getMulticastGroup(String userName) throws RemoteException {
		// TODO Auto-generated method stub
		return null;
	}

}
