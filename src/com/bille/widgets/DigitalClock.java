package com.bille.widgets;

import com.trolltech.qt.core.*;
import com.trolltech.qt.gui.*;

public class DigitalClock extends QLCDNumber {
	public DigitalClock(QWidget parent) {
		super(parent);
		
		setSegmentStyle(SegmentStyle.Flat);
		this.setStyleSheet("border: none;");

		QTimer timer = new QTimer(this);
		timer.timeout.connect(this, "showTime()");
		timer.start(1000);

		showTime();

		resize(150, 48);
	}

	public void showTime() {
		QTime time = QTime.currentTime();
		StringBuffer text = new StringBuffer(time.toString("hh:mm"));
		if ((time.second() % 2) == 0)
			text.setCharAt(2, ' ');
		display(text.toString());
	}
}