package com.bille;

public enum PaymentType {
	CARD ("CARD"), CASH ("CASH");
	
	private PaymentType(String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}
	
	private String val;
}
