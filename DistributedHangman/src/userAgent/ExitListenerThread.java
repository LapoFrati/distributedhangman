package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class ExitListenerThread extends Thread{
	PrintWriter out;
	BufferedReader stdIn, in;
	AbstractUserLauncher launcher;
	
	public ExitListenerThread(PrintWriter out, BufferedReader in, BufferedReader stdIn, AbstractUserLauncher launcher){
		this.in = in;
		this.out = out;
		this.stdIn = stdIn;
		this.launcher = launcher;
	}
	
	public void run(){
		try{
			while(true){
				try{
					if(stdIn.ready()){
						if(stdIn.readLine().matches("exit"))
							launcher.notifyExit();
						else
							System.out.println("type \"exit\" to quit.");
						
					} else {
						Thread.sleep(400);
					}
				}catch (IOException e){}
			}
		} catch (InterruptedException e){ /* Terminating ExitListener */ }
	}
}
