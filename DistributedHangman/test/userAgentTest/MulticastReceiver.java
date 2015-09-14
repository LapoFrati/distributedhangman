package userAgentTest;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.net.MulticastSocket;
import java.nio.charset.Charset;

public class MulticastReceiver {

	@SuppressWarnings("unused")
	public static void main(String[] args) throws IOException {
		String msg = "Hello";
		 InetAddress group = InetAddress.getByName("228.5.6.7");
		 InetAddress iface = InetAddress.getByName("192.168.5.2");
		 InetAddress IP=InetAddress.getLocalHost();
		 System.out.println("IP of my system is := "+IP.getHostAddress());
		 MulticastSocket s = new MulticastSocket(6789);
		 s.setInterface(iface);
		 s.joinGroup(group);
		 byte[] buf = new byte[1000];
		 DatagramPacket recv = new DatagramPacket(buf, buf.length);
		 s.receive(recv);
		 String v = new String( buf, Charset.forName("UTF-8") );
		 System.out.println(v);
		 s.close();
	}

}