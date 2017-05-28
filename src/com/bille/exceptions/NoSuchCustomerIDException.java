package com.bille.exceptions;

public class NoSuchCustomerIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such customer found";

}
