package registryTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;
import java.rmi.server.ServerNotActiveException;

import userAgent.UserLogin;

public class LaunchMaster {
	public static void main(String[] args) throws ClassNotFoundException, IOException, NotBoundException, ServerNotActiveException {
		String inputString = "Lapo\nFrati\nm\n2\n";
		InputStream inStream = new ByteArrayInputStream(inputString.getBytes());
		System.setIn(inStream);
		UserLogin.main(null);
	}
}