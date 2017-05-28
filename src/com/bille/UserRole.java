package com.bille;

public enum UserRole {
	ADMIN ("ADMIN"), STAFF ("STAFF");
	
	private UserRole(String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}
	
	private String val;
}
