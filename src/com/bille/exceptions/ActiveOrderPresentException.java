package com.bille.exceptions;

public class ActiveOrderPresentException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "Active orders are present";

}
