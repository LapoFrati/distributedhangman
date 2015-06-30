package userAgentTest;

import java.io.IOException;

import userAgent.MasterWorker;

public class MasterAgent {
	public static void main(String[] args) throws IOException {
		MasterWorker master = new MasterWorker("test", "224.0.0.0", "ape", 5);
		master.startGame();
	}
}
