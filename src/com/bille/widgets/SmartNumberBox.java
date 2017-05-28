package com.bille.widgets;

import com.trolltech.qt.QSignalEmitter.Signal1;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;

public class SmartNumberBox extends QWidget {
	
	public SmartNumberBox(QWidget parent) {
		super(parent);
		this.setObjectName("SmartNumberBox");
		
		initWidgets();
		doSignals();
		
		spinBox.setFixedHeight(35);
	}
	
	private void doSignals() {
		showBtn.clicked.connect(this, "showNumpad()");
		spinBox.valueChanged.connect(this, "changed()");
	}
	
	private void showNumpad() {
		spinBox.setValue(VirtualNumpad.getInt(this, VirtualNumpad.INTEGER, spinBox.value()));
	}
	
	private void changed() {
		changed.emit(this);
	}
	
	public int getVal() {
		return spinBox.value();
	}
	
	private void initWidgets() {
		placeholderLab.setProperty("is_placeholder", true);
		placeholderLab.hide();
		
		mainLayout.addWidget(placeholderLab, 0, 0, new Alignment(
				AlignmentFlag.AlignLeft, AlignmentFlag.AlignTop));
		mainLayout.addWidget(spinBox, 1, 0, 1, 1, new Alignment(AlignmentFlag.AlignTop));
		mainLayout.addWidget(showBtn, 1, 1, 1, 1, new Alignment(AlignmentFlag.AlignTop));
		
		this.setLayout(mainLayout);
	}
	
	public void setPlaceholder(String txt) {
		placeholderLab.setText(txt);
		placeholderLab.show();
	}
	
	public void setValue(int val) {
		spinBox.setValue(val);
	}
	
	public void setMin(int min) {
		spinBox.setMinimum(min);
	}

	private QGridLayout mainLayout = new QGridLayout(this);
	private QSpinBox spinBox = new QSpinBox(this);
	private ShowButton showBtn = new ShowButton(this);
	private QLabel placeholderLab = new QLabel(this);
	
	public Signal1<SmartNumberBox> changed = new Signal1<SmartNumberBox>();
}
