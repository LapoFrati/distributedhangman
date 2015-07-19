package registry;

import java.io.PrintWriter;
import java.net.Socket;

public class ConnectionTimeoutThread extends Thread {
	Socket socket;
	long currentTime, milliSecondsToSleep;
	PrintWriter out;
	public volatile boolean disableInterrupt = false;
	AbstractGameCreation gameCreation;
	
	public ConnectionTimeoutThread(PrintWriter out, Socket socket, long secondsToSleep,  AbstractGameCreation gameCreation) {
		this.socket = socket;
		this.currentTime = System.currentTimeMillis(); 
		this.milliSecondsToSleep = secondsToSleep*1000;
		this.gameCreation = gameCreation;
		this.out = out;
	}
	
	@Override
	public void run() {
		try {
			Thread.sleep(milliSecondsToSleep);
		} catch (InterruptedException e){ }
		
		if(!disableInterrupt){
			gameCreation.notifyTimeout();
		}
	}
	
	public void stopTimeout(){
		disableInterrupt = true;
		interrupt();
	}
}
