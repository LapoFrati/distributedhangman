package registryTest;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.rmi.NotBoundException;

import userAgent.User;

public class LaunchMaster {
	public static void main(String[] args) throws ClassNotFoundException, IOException, NotBoundException {
		String inputString = "Lapo\nFrati\nm\n2\n";
		InputStream inStream = new ByteArrayInputStream(inputString.getBytes());
		System.setIn(inStream);
		User.main(null);
	}
}
