package com.bille.exceptions;

public class CustomerIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Customer already exists";

}
