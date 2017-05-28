package com.bille.exceptions;

public class NoSuchItemIDException extends BilleException {
	
	public String getMessage() {
		return msg;
	}
	
	private String msg = "No such item found";

}
