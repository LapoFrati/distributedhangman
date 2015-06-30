package userAgent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketTimeoutException;

import messages.JSONCodes;

import org.jasypt.exceptions.EncryptionOperationNotPossibleException;
import org.jasypt.util.text.BasicTextEncryptor;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

public class MulticastSocketHandler {
	private MulticastSocket socket;
	private InetAddress group;
	private int port;
	private BasicTextEncryptor textEncryptor;
	private String role;
	private JSONObject latestMessage;
	

	
	public MulticastSocketHandler(InetAddress group, int port, String password, int timeout, String role) throws IOException {
		this.group = group;
		this.port = port;
		this.role = role;
		socket = new MulticastSocket(port);
		socket.joinGroup(group);
		socket.setSoTimeout(timeout*1000); // set .receive()'s timeout
		textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
	}
	
	public void send(JSONObject msg) throws IOException {
		String encryptedString = textEncryptor.encrypt(msg.toJSONString());
		byte[] buf = encryptedString.getBytes();
		latestMessage = msg; // store the message to resend it if needed
		synchronized (socket) {
			socket.send(new DatagramPacket(buf, buf.length, group, port));
		}
	}
	
	public JSONObject guesserReceive() throws IOException {
		byte[] buf = new byte[512];
		JSONObject result = null;
		boolean newMessageReady = false;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String encryptedMessage, decryptedMessage;
		
		while(!newMessageReady){
			try{
				synchronized (socket) {
					socket.receive(packet);
				}
			} catch(SocketTimeoutException e){
				System.out.println("Receive's timeout expired. Trying again.");
				this.send(latestMessage); // timeout expired re-send message and continue waiting for reply
				continue;
			}
			
			encryptedMessage = new String(packet.getData(), 0, packet.getLength());
			
			try{
				decryptedMessage = textEncryptor.decrypt(encryptedMessage);
				result = (JSONObject) new JSONParser().parse(decryptedMessage);
				if(((String)result.get(JSONCodes.role)).equals(role)){
					continue; // received message from the wrong source
				} else {
					newMessageReady = true;
				}
			} catch (EncryptionOperationNotPossibleException | ParseException e) {
				// received message was not correctly encrypted or not a valid JSON -> discard it
				continue;
			}
		}
		return result;
	}
	
	public JSONObject masterReceive() throws IOException {
		byte[] buf = new byte[512];
		JSONObject result = null;
		boolean newMessageReady = false;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String encryptedMessage, decryptedMessage;
		
		while(!newMessageReady){
			try{
				synchronized (socket) {
					socket.receive(packet);
				}
			} catch(SocketTimeoutException e){
				System.out.println("Master's receive's timeout expired. Guessers left.");
				return null; // notify the master returning a null value
			}
			
			encryptedMessage = new String(packet.getData(), 0, packet.getLength());
			try{
				decryptedMessage = textEncryptor.decrypt(encryptedMessage);
				result = (JSONObject) new JSONParser().parse(decryptedMessage);
				if(((String)result.get(JSONCodes.role)).equals(role)){
					continue; // received message from the wrong source
				} else {
					System.out.println(decryptedMessage);
					newMessageReady = true;
				}
			} catch (EncryptionOperationNotPossibleException | ParseException e) {
				// received message was not correctly encrypted or not a valid JSON -> discard it
				e.printStackTrace();
				continue;
			}
		}
		return result;
	}
	
	public void close() throws IOException {
		synchronized (socket) {
			socket.leaveGroup(group);
			socket.close();
		}
	}
}
