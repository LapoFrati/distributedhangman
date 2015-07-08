package registry;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class MasterConnectionListener extends Thread {
	ServerSocket masterSocket;
	int timeoutLength;
	
	public MasterConnectionListener(String masterServerPort, int timeoutLength){
		this.timeoutLength = timeoutLength;
		
		try {
			masterSocket = new ServerSocket(Integer.parseInt(masterServerPort));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run(){
		Executor myPool = Executors.newFixedThreadPool(10); // setting the pool size to 10 limits the number of simultaneous open games to 10
		
        try{
        	while(true){
        		myPool.execute(new MasterCreationThread(masterSocket.accept(), timeoutLength));
        		}
        } catch (IOException e) {
			e.printStackTrace();
		}finally{
        	try {
				masterSocket.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
        }
	}
}
