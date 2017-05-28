package com.bille.exceptions;

public class CustomerPhoneNumExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "A customer with the same phone no. already exists";

}
