package com.bille.exceptions;

public class ProductCodeExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "A product with the same code already exists";

}
