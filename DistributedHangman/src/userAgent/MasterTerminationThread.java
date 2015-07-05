package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

import messages.JSONCodes;

import org.json.simple.JSONObject;

public class MasterTerminationThread extends Thread {
	private BufferedReader stdIn;
	private Thread threadToTerminate;
	private MulticastSocketHandler socket;
	private JSONObject closingMessage;
	
	@SuppressWarnings("unchecked")
	public MasterTerminationThread(Thread threadToTerminate, MulticastSocketHandler socket){
		this.threadToTerminate = threadToTerminate;
		this.socket = socket;
		stdIn = new BufferedReader( new InputStreamReader(System.in) );
		closingMessage = new JSONObject();
		closingMessage.put(JSONCodes.role, JSONCodes.master);
		closingMessage.put(JSONCodes.gameStatus, JSONCodes.masterLeft);
	}
	
	public void run(){
		
			try {
				while(!stdIn.readLine().matches("exit")){}
				threadToTerminate.interrupt(); // used to notify game's closure
				socket.send(closingMessage); // warn guessers
				socket.close(); // block further message sending/waiting
				
			} catch (IOException e) {
				e.printStackTrace();
			} catch (NullPointerException e){
				// System.in has been closed. Terminate the thread.
			}
	}
	
	public void terminate() throws IOException{
		System.in.close();
	}
}
