package UserAgent;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class UserConfiguration {
	JSONObject config;
	
	public UserConfiguration( String file) throws IOException{
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
}
