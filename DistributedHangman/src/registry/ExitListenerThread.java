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
		try{
			while(exitReceived == false){
				try{
					if(in.ready()){
						if(in.readLine().matches("exit")){
							exitReceived = true;
							gameCreation.notifyExit();
						}
						else
							System.out.println("type \"exit\" to quit.");
						
					} else {
						Thread.sleep(200);
					}
				}catch (IOException e){}
			}
		} catch (InterruptedException e){ /* Terminating ExitListener */ }
	}
}
