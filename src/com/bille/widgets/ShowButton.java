package com.bille.widgets;

import com.trolltech.qt.core.Qt.FocusPolicy;
import com.trolltech.qt.gui.*;

public class ShowButton extends QPushButton {

	public ShowButton(QWidget parent) {
		super(parent);
		this.setObjectName("ShowButton");
		
		this.setFocusPolicy(FocusPolicy.NoFocus);
		this.setFixedSize(37, 32);
	}
	
}
