package com.bille.exceptions;

public class TaxNameExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Tax name already exists";
}
