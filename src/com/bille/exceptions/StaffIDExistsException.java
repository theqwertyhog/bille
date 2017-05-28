package com.bille.exceptions;

public class StaffIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Staff already exists";

}
