package userAgentTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;

public class MulticastSender {

	public static void main(String[] args) throws IOException {
		String msg = "Hello";
		 InetAddress group = InetAddress.getByName("228.5.6.7");
		 MulticastSocket s = new MulticastSocket(6789);
		 InetAddress iface = InetAddress.getByName("192.168.5.3");
		 s.setInterface(iface);
		 s.joinGroup(group);
		 DatagramPacket hi = new DatagramPacket(msg.getBytes(), msg.length(),
		                             group, 6789);
		 s.send(hi);
		 s.close();
	}

}
