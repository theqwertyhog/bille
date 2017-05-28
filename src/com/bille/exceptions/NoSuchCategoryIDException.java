package com.bille.exceptions;

public class NoSuchCategoryIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such category found";

}
