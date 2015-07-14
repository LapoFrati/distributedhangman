package registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

public abstract class AbstractGameCreation extends Thread {
	private boolean exitListenerStopped, 
					connectionTimeoutStopped;
	protected ConnectionTimeoutThread connectionTimeout;
	protected ExitListenerThread exitListener;
	protected PrintWriter out;
	protected BufferedReader in;
	protected Socket socket;
	protected long timeout;
	
	
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
		exitListener = new ExitListenerThread(this, in);
		connectionTimeout = new ConnectionTimeoutThread(out, socket, timeout, this);
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
			exitListener.stopListener();
		}
	}
	
	public abstract void run();
	public abstract void notifyExit();
	public abstract void notifyTimeout();
	public abstract void checkState();
}
