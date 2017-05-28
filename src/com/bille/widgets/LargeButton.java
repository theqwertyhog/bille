package com.bille.widgets;

import com.bille.ui.GradientColor;
import com.trolltech.qt.gui.*;

public class LargeButton extends QPushButton {
	
	public LargeButton(QWidget parent, String label, GradientColor background) {
		super(parent);
		this.setObjectName("LargeButton");
		this.setProperty("color_code", background);
		this.setProperty("is_bille", true);
		
		this.setFixedSize(80, 80);
				
		this.setText(label);
		
		doSignals();
	}
	
	private void doSignals() {
		this.clicked.connect(this, "emitClickedUpon()");
	}
	
	private void emitClickedUpon() {
		clickedUpon.emit(this.text());
	}
	
	public Signal1<String> clickedUpon = new Signal1<String>();

}
