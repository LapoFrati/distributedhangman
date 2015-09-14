package userAgent;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.net.SocketException;
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
	
	
	public MulticastSocketHandler(InetAddress group, int port, String password, int timeout, String role, String interf) throws IOException {
		this.group = group;
		this.port = port;
		this.role = role;
		InetAddress iface = InetAddress.getByName(interf);
		socket = new MulticastSocket(port);
		socket.setSoTimeout(timeout*1000);
		
		socket.setInterface(iface);
		socket.joinGroup(group);
		
		textEncryptor = new BasicTextEncryptor();
		textEncryptor.setPassword(password);
	}
	
	/**
	 * Method used to send a JSON to the multicast address associated to the current instance of this class 
	 * @param msg
	 * @throws IOException
	 */
	public void send(JSONObject msg) throws IOException {
		String encryptedString = textEncryptor.encrypt(msg.toJSONString());
		byte[] buf = encryptedString.getBytes();
		
		try{
			socket.send(new DatagramPacket(buf, buf.length, group, port));
		}catch(SocketException e){
			System.out.println("Socket Closed");
		}
		
	}
	
	/**
	 * Method to receive a message from the multicast address. It checks that the sender is not the guesser itself and takes care of decrypting it.
	 * @return the message received from the master or another guesser
	 * @throws IOException
	 */
	public JSONObject guesserReceive() throws IOException {
		byte[] buf = new byte[512];
		JSONObject result = null;
		boolean newMessageReady = false;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String encryptedMessage, decryptedMessage;
		
		while(!newMessageReady){
			try{
				
				socket.receive(packet);
				
			} catch(SocketException e){
				// socket has been closed
				result = null;
				break;
			} catch (SocketTimeoutException e){
				// the long timeout has expired
				result = null;
				break;
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
	
	/**
	 * Method used by the master to receive the message from the guessers.
	 * @return the message received from the guessers.
	 * @throws IOException
	 */
	public JSONObject masterReceive() throws IOException {
		byte[] buf = new byte[512];
		JSONObject result = null;
		boolean newMessageReady = false;
		DatagramPacket packet = new DatagramPacket(buf, buf.length);
		String encryptedMessage, decryptedMessage;
		
		while(!newMessageReady){
			try{
					socket.receive(packet);
				
			} catch(SocketTimeoutException e){
				System.out.println("Master's receive's timeout expired. Guessers left.");
				return null; // notify the master returning a null value
			} catch(SocketException e){
				if(Thread.interrupted()){
					System.out.println("GAME OVER"); // master requests game termination
					System.exit(0);
				}
				return null;
			}
			
			encryptedMessage = new String(packet.getData(), 0, packet.getLength());
			try{
				decryptedMessage = textEncryptor.decrypt(encryptedMessage);
				result = (JSONObject) new JSONParser().parse(decryptedMessage);
				if(((String)result.get(JSONCodes.role)).equals(role)){
					continue; // received message from the wrong source
				} else {
					//System.out.println(decryptedMessage);
					System.out.println((String)result.get(JSONCodes.senderNick) + ": "+ (String)result.get(JSONCodes.guess));
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
	
	/**
	 * Method used to close the multicast socket.
	 * @throws IOException
	 */
	public void close() throws IOException {
		socket.leaveGroup(group);
		socket.close();
		
	}
}
