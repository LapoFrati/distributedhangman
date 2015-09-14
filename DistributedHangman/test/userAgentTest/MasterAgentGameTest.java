package userAgentTest;

import java.io.IOException;

import userAgent.MasterWorker;

public class MasterAgentGameTest {
	public static void main(String[] args) throws IOException {
		MasterWorker master = new MasterWorker("test", "224.0.0.1", "ape", 5, 1, "192.168.5.2");
		master.startGame();
	}
}