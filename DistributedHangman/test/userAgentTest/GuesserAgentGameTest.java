package userAgentTest;

import java.io.IOException;
import java.net.UnknownHostException;

import userAgent.GuesserReceiver;

public class GuesserAgentGameTest {
	public static void main(String[] args) throws UnknownHostException, IOException {
		GuesserReceiver guesser = new GuesserReceiver("Lapo", "test", "224.0.0.1", "192.168.5.3");
		guesser.startGame();
	}
}
