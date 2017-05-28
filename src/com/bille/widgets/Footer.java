package com.bille.widgets;

import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class Footer extends QFrame {
	
	public Footer(QWidget parent) {
		super(parent);
		this.setObjectName("Footer");
		
		initWidgets();
	}
	
	private void initWidgets() {
		mainLay.addWidget(copyrightLab, 0, new Alignment(AlignmentFlag.AlignCenter));
	}
	
	private QVBoxLayout mainLay = new QVBoxLayout(this);
	private QLabel copyrightLab = new QLabel("Bille 2014", this);

}
