package com.bille.exceptions;

public class NoSuchStaffIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such staff found";

}
