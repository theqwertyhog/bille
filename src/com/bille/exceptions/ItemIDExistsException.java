package com.bille.exceptions;

public class ItemIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Item already exists";

}
