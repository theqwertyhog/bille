package com.bille.exceptions;

public class CategoryIDExistsException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Category already exists";

}
