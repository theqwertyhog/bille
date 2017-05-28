package com.bille.exceptions;

public class ServerConnectionException extends BilleException {

	public String getMessage() {
		return msg;
	}
	
	private String msg = "Error reading from the server.";
	
}
