package userAgent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

import org.jasypt.util.text.BasicTextEncryptor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

public class MulticastSocketHandler {
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	BasicTextEncryptor textEncryptor;
	

	
	public MulticastSocketHandler(InetAddress group, int port, String password) throws IOException {
		this.group = group;
		this.port = port;
		socket = new MulticastSocket(port);
		socket.joinGroup(group);
		textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
	}
	
	public void send(JSONObject msg) throws IOException {
		String encryptedString = textEncryptor.encrypt(msg.toJSONString());
		byte[] buf = encryptedString.getBytes();
		socket.send(new DatagramPacket(buf, buf.length, group, port));
	}
	
	public JSONObject receive() throws IOException {
		byte[] buf = new byte[512];
		JSONObject result = null;
		boolean parsingSuccessful = false;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		
		while(!parsingSuccessful)
			socket.receive(packet);
			try{
				result = (JSONObject) new JSONParser().parse(new String(packet.getData()));
				parsingSuccessful = true;
			} catch (Exception e) {
				// received message not correctly encrypted
			}
			
		return result;
	}
	
	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
	}
}
