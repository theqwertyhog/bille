package com.bille.widgets;

import com.bille.ui.GradientColor;
import com.trolltech.qt.QSignalEmitter.Signal1;
import com.trolltech.qt.gui.*;

public class RectButton extends QPushButton {
	
	public RectButton(QWidget parent, String label, GradientColor background) {
		super(parent);
		this.setObjectName("RectButton");
		this.setProperty("color_code", background);
		this.setProperty("is_bille", true);
		
		this.setFixedSize(70, 50);
		
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
