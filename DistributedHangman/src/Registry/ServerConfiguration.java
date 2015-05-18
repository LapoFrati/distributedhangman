package Registry;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerConfiguration {
	JSONObject config;
	public ServerConfiguration( String file ) throws IOException{
		String json;
		try (BufferedReader br = new BufferedReader(new FileReader(file))) {
		    if ((json = br.readLine()) == null) 
		    	throw new ConfigurationException("Couldn't read config file.");
		    
		} catch (FileNotFoundException e){
			throw new ConfigurationException("Couldn't find config file.");
		}
		
		try {
			config = (JSONObject) new JSONParser().parse(json);
		} catch (ParseException e) {
			throw new ConfigurationException("Couldn't parse the json file.");
		}
	}
	
	public String getServerIp(){
		String serverIP;
		
		if((serverIP = (String)config.get("SERVER_IP")) == null)
			throw new ConfigurationException("Couldn't find server's ip.");
		
		return serverIP;
	}
	
	public String getRegistryName(){
		String registryName;
		
		if((registryName = (String)config.get("REGISTRY_NAME")) == null)
			throw new ConfigurationException("Couldn't find registry's name.");
		
		return registryName;
	}
	
	public String getRegistryPort(){
		String registryPort;
		
		if((registryPort = (String)config.get("REGISTRY_PORT")) == null)
			throw new ConfigurationException("Couldn't find registry's port.");
		
		return registryPort;
	}
	
	public String getServerPort(){
		String serverPort;
		
		if((serverPort = (String)config.get("SERVER_PORT")) == null)
			throw new ConfigurationException("Couldn't find server's port.");
		
		return serverPort;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,UserInfo> getRegisterdUserInfo() throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		Map<String, UserInfo> users = Collections.synchronizedMap(new HashMap<String, UserInfo>());
		
		ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
		retrievedInfo = (List<Pair>) input.readObject();
		input.close();
	
		for( Pair pair : retrievedInfo){
			users.put(pair.getUserName(), new UserInfo(pair.getEncryptedPassword()));
		}
		
		return users;
	}
	
	public void saveUsers(String userName, String encryptedPassword) throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		
		ObjectInputStream input = new ObjectInputStream( new FileInputStream("users.data"));
		retrievedInfo = (List<Pair>) input.readObject();
		input.close();
	
		retrievedInfo.add(new Pair(userName, encryptedPassword));
		
		// File with old name
	    File file = new File("users.data");
	    // File with new name
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
	
	/*
	@SuppressWarnings("unchecked")
	public List<InetAddress> getMulticastAddresses() throws UnknownHostException{
		List<InetAddress> multicastAddressesConverted = Collections.synchronizedList(new LinkedList<InetAddress>());
		List<String> multicastAddressesString;
		
		if((multicastAddressesString = (LinkedList<String>)config.get("MULTICAST_ADDRESSES")) == null)
			throw new ConfigurationException("Couldn't find the list of multicastAddresses.");
		
		for(String addr : multicastAddressesString){
			multicastAddressesConverted.add(InetAddress.getByName(addr));
		}
		
		return multicastAddressesConverted;
	}
	*/
	
	public int getMaxNumberOfGames(){
		String maxNumberOfGames;
		
		if((maxNumberOfGames = (String)config.get("MAX_GAMES")) == null)
			throw new ConfigurationException("Couldn't find the max number of games.");
		
		return Integer.parseInt(maxNumberOfGames);
	}
}

