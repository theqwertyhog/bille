package com.bille.widgets;

import com.trolltech.qt.gui.QFrame;
import com.trolltech.qt.gui.QWidget;

public class VLine extends QFrame {

	public VLine(QWidget parent) {
		super(parent);

		setFrameShape(QFrame.Shape.VLine);
		setFrameShadow(QFrame.Shadow.Raised);
	}

}
