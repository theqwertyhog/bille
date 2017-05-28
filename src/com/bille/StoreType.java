package com.bille;

public enum StoreType {
	RESTO ("RESTO"), RETAIL ("RETAIL");
	
	private StoreType(String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}
	
	private String val;
}
