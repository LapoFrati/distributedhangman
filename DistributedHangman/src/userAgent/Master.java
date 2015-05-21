package userAgent;

import java.io.BufferedReader;
import java.io.PrintWriter;

public class Master {
	PrintWriter out;
	BufferedReader in;
	String userName;
	
	public Master(PrintWriter out, BufferedReader in, String userName){
		this.out = out;
		this.in = in;
		this.userName = userName;
	}
	
	public void createGame(){
		
	}
}
