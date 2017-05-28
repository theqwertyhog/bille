package com.bille;

public class ShutdownHook extends Thread {

	public void run() {
		System.out.println("Shutting down...");
		
		Application.getInstance().getMainDB().close();
		Application.getInstance().getLiteDB().close();
		Application.releaseInstance();
		
		System.out.println("Bye, bye!");
	}

}
