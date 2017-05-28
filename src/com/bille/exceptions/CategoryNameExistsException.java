package com.bille.exceptions;

public class CategoryNameExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "A category with the same name already exists";

}
