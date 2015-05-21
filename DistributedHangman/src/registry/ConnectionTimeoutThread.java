package registry;

import java.io.IOException;
import java.net.Socket;

import messages.TCPmsg;

public class ConnectionTimeoutThread extends Thread {
	Socket socketToBeClosed;
	long currentTime, alarmTime;
	public volatile boolean alarmActivated = true;
	
	public ConnectionTimeoutThread(Socket socket, long secondsToSleep) {
		this.socketToBeClosed = socket;
		this.currentTime = System.currentTimeMillis(); 
		this.alarmTime = currentTime + secondsToSleep*1000;
	}
	
	@Override
	public void run() {
		byte[] closingMessage = TCPmsg.connectionClosed.getBytes();
		// TODO: analyze interaction between threads to avoid inconsistencies
		while(alarmActivated){
			try {
					Thread.sleep(alarmTime - currentTime);
				alarmActivated = false;
			} catch (InterruptedException e) {
				if(alarmActivated){
					currentTime = System.currentTimeMillis();
					if(currentTime > alarmTime)
						alarmActivated = false;
				}
			}
		}
		
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
