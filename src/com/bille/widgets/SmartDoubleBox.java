package com.bille.widgets;

import com.bille.MainDB;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;

public class SmartDoubleBox extends QWidget {

	public SmartDoubleBox(QWidget parent) {
		super(parent);
		this.setObjectName("SmartDoubleBox");

		initWidgets();
		doSignals();
		
		spinBox.setFixedHeight(35);
	}

	private void doSignals() {
		showBtn.clicked.connect(this, "showNumpad()");
		spinBox.valueChanged.connect(this, "changed()");
	}

	private void showNumpad() {
		spinBox.setValue(VirtualNumpad.getDouble(this, VirtualNumpad.DECIMAL,
				spinBox.value()));
	}

	private void changed() {
		changed.emit(this);
	}

	public double getVal() {
		return spinBox.value();
	}

	public void disableDecimals() {
		spinBox.setDecimals(0);
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

	public void setValue(double val) {
		spinBox.setValue(val);
	}

	public void setMin(double min) {
		spinBox.setMinimum(min);
	}

	public void setMax(double max) {
		spinBox.setMaximum(max);
	}

	private QGridLayout mainLayout = new QGridLayout(this);
	private QDoubleSpinBox spinBox = new QDoubleSpinBox(this);
	private ShowButton showBtn = new ShowButton(this);
	private QLabel placeholderLab = new QLabel(this);

	public Signal1<SmartDoubleBox> changed = new Signal1<SmartDoubleBox>();
	public Signal1<Double> valueChanged = spinBox.valueChanged;
}