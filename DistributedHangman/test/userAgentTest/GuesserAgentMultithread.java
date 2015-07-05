package userAgentTest;

import java.io.IOException;
import java.net.UnknownHostException;

import userAgent.GuesserReceiver;

public class GuesserAgentMultithread {
	public static void main(String[] args) throws UnknownHostException, IOException {
		GuesserReceiver guesser = new GuesserReceiver("Lapo", "test", "224.0.0.0");
		guesser.startGame();
	}
}
