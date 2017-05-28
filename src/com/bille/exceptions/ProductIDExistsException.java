package com.bille.exceptions;

public class ProductIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Product already exists";

}
