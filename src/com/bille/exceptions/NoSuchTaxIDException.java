package com.bille.exceptions;

public class NoSuchTaxIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such tax found";

}
