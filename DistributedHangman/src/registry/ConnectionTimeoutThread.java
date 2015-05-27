package registry;

import java.io.IOException;
import java.net.Socket;

import org.json.simple.JSONObject;

import messages.JSONCodes;

public class ConnectionTimeoutThread extends Thread {
	Socket socketToBeClosed;
	long currentTime, milliSecondsToSleep;
	Thread threadToWakeup;
	public volatile boolean disableInterrupt = false;
	
	public ConnectionTimeoutThread(Socket socket, long secondsToSleep, Thread thread) {
		this.socketToBeClosed = socket;
		this.currentTime = System.currentTimeMillis(); 
		this.milliSecondsToSleep = secondsToSleep*1000;
		this.threadToWakeup = thread;
	}
	
	@SuppressWarnings("unchecked")
	@Override
	public void run() {
		JSONObject closingJSON = new JSONObject();
		closingJSON.put(JSONCodes.message, JSONCodes.connectionClosed);
		byte[] closingMessage = closingJSON.toJSONString().getBytes();
		
			try {
				Thread.sleep(milliSecondsToSleep);
			} catch (InterruptedException e){}
		
		if(!disableInterrupt)
			threadToWakeup.interrupt();
		
		try {
			socketToBeClosed.getOutputStream().write(closingMessage);
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		try {
			socketToBeClosed.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
}
