package registry;

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
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import messages.JSONCodes;

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
	
	public String getJsonField(String field){
		String requestedField;
		
		if((requestedField = (String)config.get(field)) == null)
			throw new ConfigurationException("Couldn't find server's "+field);
		return requestedField;
	}
	
	@SuppressWarnings("unchecked")
	public Map<String,UserInfo> getRegisterdUserInfo() throws FileNotFoundException, IOException, ClassNotFoundException{
		List<Pair> retrievedInfo;
		Map<String, UserInfo> users = new ConcurrentHashMap<String, UserInfo>();
		
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
	
	public int getMaxNumberOfGames(){
		String maxNumberOfGames;
		
		if((maxNumberOfGames = (String)config.get("MAX_GAMES")) == null)
			throw new ConfigurationException("Couldn't find the max number of games.");
		
		return Integer.parseInt(maxNumberOfGames);
	}
	
	public static long ipToLong(String ipAddress) {
        long result = 0;
        String[] atoms = ipAddress.split("\\.");

        for (int i = 3; i >= 0; i--) {
            result |= (Long.parseLong(atoms[3 - i]) << (i * 8));
        }

        return result & 0xFFFFFFFF;
    }

    public static String longToIp(long ip) {
        StringBuilder sb = new StringBuilder(15);

        for (int i = 0; i < 4; i++) {
            sb.insert(0, Long.toString(ip & 0xff));

            if (i < 3) {
                sb.insert(0, '.');
            }

            ip >>= 8;
        }

        return sb.toString();
    }
}

