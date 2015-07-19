package registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import messages.JSONCodes;

import org.json.simple.JSONObject;

public abstract class AbstractGameCreation extends Thread {
	private boolean exitListenerStopped, 
					connectionTimeoutStopped;
	protected ConnectionTimeoutThread connectionTimeout;
	protected ExitListenerThread exitListener;
	protected PrintWriter out;
	protected BufferedReader in;
	protected Socket socket;
	protected long timeout;
	protected JSONObject closingJSON;
	protected boolean exitReceived, timeoutExpired;
	
	
	
	@SuppressWarnings("unchecked")
	public AbstractGameCreation(Socket socket, long timeout){
		this.socket = socket;
		this.timeout = timeout;
		try {
			out = new PrintWriter(socket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
        try {
			in = new BufferedReader( new InputStreamReader( socket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		exitListenerStopped = false;
		connectionTimeoutStopped = false;
		exitReceived = false;
		timeoutExpired = false;
		exitListener = new ExitListenerThread(this, in);
		connectionTimeout = new ConnectionTimeoutThread(out, socket, timeout, this);
		closingJSON = new JSONObject();
		closingJSON.put(JSONCodes.message, JSONCodes.connectionClosed);
	}
	
	protected void stopTimeout(){
		if(connectionTimeoutStopped == false){
			connectionTimeoutStopped = true;
			connectionTimeout.stopTimeout();
		}
	}
	
	protected void stopListener(){
		if(exitListenerStopped == false){
			exitListenerStopped = true;
			exitListener.interrupt();
		}
	}
	
	public abstract void run();
	public abstract void notifyExit();
	public abstract void notifyTimeout();
}
