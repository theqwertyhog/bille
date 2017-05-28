package com.bille.exceptions;

public class ItemCodeExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "An item with the same code already exists";

}
