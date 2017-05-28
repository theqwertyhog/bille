package com.bille;

public enum RestoOrderType {
	DINE_IN ("DINE_IN"), TAKEAWAY ("TAKEAWAY"), DELIVERY ("DELIVERY");
	
	private RestoOrderType(String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}
	
	private String val;
}
