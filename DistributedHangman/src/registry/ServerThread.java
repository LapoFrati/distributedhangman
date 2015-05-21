package registry;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;

import messages.JSONCodes;
import messages.TCPmsg;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class ServerThread extends Thread{
	private Socket socket = null;
	private long timeout;
	
	public ServerThread( Socket socket, long timeout){
		super("ServerThread");
		this.socket = socket;
		this.timeout = timeout;
	}
	
	public void run() {
		String role;
		// start timeout to close the socket
		ConnectionTimeoutThread connectionTimeout = new ConnectionTimeoutThread(socket, timeout);
		connectionTimeout.start();
		
        try (
            PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
            BufferedReader in = new BufferedReader(
                new InputStreamReader(
                    socket.getInputStream()));
        ) {
        	
        	System.out.println("Thread handling socket.");
        
        try {
			JSONObject messageFromClient = (JSONObject) new JSONParser().parse(in.readLine());
			role = (String) messageFromClient.get(JSONCodes.role);
			System.out.println(role);
			
			out.println(TCPmsg.ack);
			
			switch(role){
				case JSONCodes.master : System.out.println("Creating master");
										break;
				case JSONCodes.guesser: System.out.println("Creating guesser");
										break;
				default: 				System.out.println("Bad JSONCodes.role's content");
			}
			
		} catch (ParseException e) {
			e.printStackTrace();
		}
        
        	try { // terminate connectionTimeout's alarm to close the socket.
        		connectionTimeout.alarmActivated = false;
        		connectionTimeout.interrupt();
				connectionTimeout.join();
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        	
        	System.out.println("joined Timeout Thread");
        	
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
