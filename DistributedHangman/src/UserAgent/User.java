package UserAgent;

import java.io.IOException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.server.ServerNotActiveException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Scanner;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import Encryption.EncryptionUtil;

public class User{
	public static void main(String[] args) throws IOException, NotBoundException, ServerNotActiveException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException {

		Scanner scanner = new Scanner(System.in);
		RegistryConfiguration config = new RegistryConfiguration("config.json");
		String 	serverIP = config.getServerIp(), 
				serverName = config.getServerName(),
				serverPort = config.getServerPort();
		
		Login reg = (Login) Naming.lookup("rmi://"+serverIP+":"+serverPort+"/"+serverName);
		
		userLogin(reg, scanner);
		
		scanner.close();
			
	}
	
	private static void userLogin(Login reg, Scanner scanner) throws RemoteException, ServerNotActiveException, InvalidKeyException, NoSuchAlgorithmException, NoSuchPaddingException, IllegalBlockSizeException, BadPaddingException{
		String userName = "foo", password = "bar";
		boolean loginError = false;
		byte[] encryptedPassword;
		
		do{	
			if(loginError){
				System.out.println("Wrong password!");
			}else{
				do{ 
					System.out.println("User Name: ");
				}while((userName = scanner.nextLine()) == null);
			}
			
			do{
				System.out.println("Password: ");
			}while((password = scanner.nextLine()) == null);
			
			encryptedPassword =  EncryptionUtil.encrypt(password, reg.getPublicKey());
			
			if(reg.isRegisteredUser(userName) == false){
				System.out.println("Registering new user: "+userName);
				reg.registerNewUser(userName, encryptedPassword);
			}
		
		}while(reg.logIn(userName, encryptedPassword) == false && (loginError = true));
	}
}
