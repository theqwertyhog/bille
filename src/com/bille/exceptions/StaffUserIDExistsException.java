package com.bille.exceptions;

public class StaffUserIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "A staff with the same user ID already exists";

}
