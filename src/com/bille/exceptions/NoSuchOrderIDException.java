package com.bille.exceptions;

public class NoSuchOrderIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such order found";

}
