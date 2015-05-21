package messages;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ReadConfigurationFile {

		JSONObject config;
		public ReadConfigurationFile( String file ) throws IOException{
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
}
