package com.bille;

public enum Unit {
	NOS("NOS"), KG("KG"), GRAMS("GRAMS"), LTR("LTR"), ML("ML");

	private Unit(final String val) {
		this.val = val;
	}
	
	public String toString() {
		return this.val;
	}
	
	private String val;
}