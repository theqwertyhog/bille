package com.bille.exceptions;

public class TaxIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Tax already exists";

}
