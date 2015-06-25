package userAgent;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MasterTerminationThread extends Thread {
	private BufferedReader stdIn;
	private Thread threadToTerminate;
	
	public MasterTerminationThread(Thread threadToTerminate){
		this.threadToTerminate = threadToTerminate;
		stdIn = new BufferedReader( new InputStreamReader(System.in) );
	}
	
	public void run(){
		
			try {
				while(!stdIn.readLine().matches("exit")){}
				threadToTerminate.interrupt();
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
}
