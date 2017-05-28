package com.bille.exceptions;

public class LocalDBException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "There was a problem trying to read from local storage";

}
