package com.bille.exceptions;

public class PasswordsDontMatchException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Passwords don't match";

}
