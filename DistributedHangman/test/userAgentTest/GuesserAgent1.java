package userAgentTest;

import java.io.IOException;
import java.net.UnknownHostException;

import userAgent.Guesser;

public class GuesserAgent1 {
	public static void main(String[] args) throws UnknownHostException, IOException {
		Guesser guesser = new Guesser("Lapo", "test", "224.0.0.0");
		guesser.startGame();
	}
}
