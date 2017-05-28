package com.bille.ui;

public enum GradientColor {
	
	GREY ("grey"), BLUE ("blue"), GREEN ("green"), YELLOW ("yellow"), RED ("red"), TRANSPARENT ("transparent");
	
	private GradientColor(String val) {
		this.val = val;
	}
	
	public String toString() {
		return val;
	}
	
	private String val;

}
