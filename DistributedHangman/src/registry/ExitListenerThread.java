package registry;

import java.io.BufferedReader;
import java.io.IOException;

public class ExitListenerThread extends Thread {
	AbstractGameCreation gameCreation;
	BufferedReader in;
	String messageReceived;
	boolean exitReceived, disableListener;
	
	public ExitListenerThread(AbstractGameCreation gameCreation, BufferedReader in) {
		this.gameCreation = gameCreation;
		this.in = in;
		exitReceived = false;
		disableListener = false;
	}
	
	public void run(){
		do{
			try {
				messageReceived = in.readLine();
			} catch (IOException e) {
				break;
			}
			if(messageReceived != null && !disableListener){
				if(messageReceived.matches("exit")){
					System.out.println("Exit Request Received");
					exitReceived = true;
					gameCreation.notifyExit();
				}
			}
		}while(exitReceived == false);
		System.out.println("ExitListener Terminated");
	}
	
	public void stopListener(){
		disableListener = true;
		exitReceived = true;
		try { in.close(); } catch (IOException e) { }
	}
}
