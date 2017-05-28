package com.bille.exceptions;

public class NoSuchProductIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such product found";

}
