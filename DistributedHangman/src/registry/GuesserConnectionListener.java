package registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class GuesserConnectionListener extends Thread{
	ServerSocket guesserSocket;
	int timeoutLength;
	
	public GuesserConnectionListener(String guesserServerPort, int timeoutLength){
		this.timeoutLength = timeoutLength;
		
		try {
			guesserSocket = new ServerSocket(Integer.parseInt(guesserServerPort));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		Executor myPool = Executors.newCachedThreadPool();
        try{
        	while(true){
        		myPool.execute(new GuesserCreationThread(guesserSocket.accept(), timeoutLength));
        		}
        } catch (IOException e) {
			e.printStackTrace();
		}finally{
        	try {
				guesserSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
		
	}
}
