package registryTest;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import org.jasypt.util.password.BasicPasswordEncryptor;

import registry.Pair;


public class ProvaSerialization {
	public static void main(String[] args) throws FileNotFoundException, IOException, ClassNotFoundException {
		/*BasicPasswordEncryptor passwordEncryptor = new BasicPasswordEncryptor();
		LinkedList <Pair> usersInfo = new LinkedList<Pair>();
	
		usersInfo.add(new Pair("Test", passwordEncryptor.encryptPassword("qwerty")));
		
		System.out.println("Original list");
		for( Pair pair : usersInfo ){
			System.out.println(pair.getUserName()+" "+pair.getEncryptedPassword());
		}
		
		ObjectOutputStream output = new ObjectOutputStream(new FileOutputStream("users.data"));
		
		output.writeObject(usersInfo);
		output.close();
		
		ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
		@SuppressWarnings("unchecked")
		LinkedList <Pair> retrievedInfo = (LinkedList<Pair>) input.readObject();
		input.close();
		
		System.out.println("Retrieved list");
		for( Pair pair : retrievedInfo ){
			System.out.println(pair.getUserName()+" "+pair.getEncryptedPassword());
		}
		
		updateData("test2", passwordEncryptor.encryptPassword("asdzxc"));
		*/
		System.out.println("users.data: ");
		displaySerialization("users.data");
		System.out.println("users.data.backup: ");
		displaySerialization("users.data.backup");
		
	}
	
	@SuppressWarnings("unchecked")
	public static void updateData(String userName, String encryptedPassword) throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		
		ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
		retrievedInfo = (List<Pair>) input.readObject();
		input.close();
	
		retrievedInfo.add(new Pair(userName, encryptedPassword));
		// File (or directory) with old name
	    File file = new File("users.data");
	    // File (or directory) with new name
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
	
	public static void displaySerialization(String file) throws FileNotFoundException, IOException, ClassNotFoundException{
		ObjectInputStream input = new ObjectInputStream( new FileInputStream(file));
		@SuppressWarnings("unchecked")
		LinkedList <Pair> retrievedInfo = (LinkedList<Pair>) input.readObject();
		input.close();

		for( Pair pair : retrievedInfo ){
			System.out.println(pair.getUserName()+" "+pair.getEncryptedPassword());
		}
	}
}