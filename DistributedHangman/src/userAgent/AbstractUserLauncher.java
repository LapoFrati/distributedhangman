package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.net.Socket;

public abstract class AbstractUserLauncher {
	protected Socket serverSocket;
	protected PrintWriter out;
    protected BufferedReader in;
    protected ExitListenerThread exitListener;
    private boolean listenerStopped;
    
	public AbstractUserLauncher(BufferedReader stdIn, String serverIP, String masterServerPort){
		try {
			serverSocket = new Socket(InetAddress.getByName(serverIP), Integer.parseInt(masterServerPort));
		} catch (NumberFormatException | IOException e) {
			e.printStackTrace();
		}
		try {
			out = new PrintWriter(serverSocket.getOutputStream(), true);
		} catch (IOException e) {
			e.printStackTrace();
		}
		try {
			in = new BufferedReader( new InputStreamReader(serverSocket.getInputStream()));
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		exitListener = new ExitListenerThread(out, in, stdIn, this);
		listenerStopped = true;
	}
	
	/**
	 * Methods that stops the exit listener
	 */
	protected void stopListener(){
		if(listenerStopped == false){ // prevents multiple interruptions
			listenerStopped = true;
			exitListener.interrupt();
		}
	}
	
	/**
	 * Method that starts the exit listener
	 */
	protected void startListener(){
		if(listenerStopped == true){ //prevents multiple initializations
			listenerStopped = false;
			exitListener.start();
		}	}
	
	public abstract void notifyExit();
	public abstract void checkState();
}
