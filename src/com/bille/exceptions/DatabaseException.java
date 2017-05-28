package com.bille.exceptions;

public class DatabaseException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "There was a problem while trying to fetch data";

}
