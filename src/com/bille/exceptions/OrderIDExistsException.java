package com.bille.exceptions;

public class OrderIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Order already exists";

}
