package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

public class MasterLauncherTerminationThread extends Thread{
	PrintWriter out;
	BufferedReader stdIn, in;
	
	public MasterLauncherTerminationThread(PrintWriter out, BufferedReader stdIn, BufferedReader in){
		this.stdIn = stdIn;
		this.in = in;
		this.out = out;
	}
	
	public void run(){
		// wait for an exit request
		boolean exitRequest = false;
		String input = "";
		
		while(exitRequest == false){
			try {
				input = stdIn.readLine();
			} catch (IOException e) {
				e.printStackTrace();
			}
			
			if(input.matches("exit")){
				exitRequest = true;
			} else {
				System.out.println("type \"exit\" to quit.");
			}
		}
		
	}
}
