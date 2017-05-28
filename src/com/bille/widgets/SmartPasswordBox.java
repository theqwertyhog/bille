package com.bille.widgets;

import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QLineEdit.EchoMode;

public class SmartPasswordBox extends QWidget {
	
	public SmartPasswordBox(QWidget parent) {
		super(parent);
		this.setObjectName("SmartPasswordBox");
		
		initWidgets();
		doSignals();
	}
	
	private void doSignals() {
		showBtn.clicked.connect(this, "showKeyboard()");
	}
	
	private void showKeyboard() {
		textBox.setText(VirtualKeyboard.getPassword(this, textBox.text()));
	}
	
	public void setPlaceholder(String text) {
		placeholderLab.setText(text);
	}
	
	public String getPassword() {
		return textBox.text();
	}
	
	public void setPassword(String pass) {
		textBox.setText(pass);
		textBox.setCursorPosition(0);
	}
	
	private void initWidgets() {
		textBox.setProperty("is_show", true);
		placeholderLab.setProperty("is_placeholder", true);
		textBox.setEchoMode(EchoMode.Password);
		
		mainLayout.addWidget(placeholderLab, 0, 0, new Alignment(AlignmentFlag.AlignLeft));
		mainLayout.addWidget(textBox, 1, 0);
		mainLayout.addWidget(showBtn, 1, 1);
		
		this.setLayout(mainLayout);
	}
	
	private QGridLayout mainLayout = new QGridLayout(this);
	private QLineEdit textBox = new QLineEdit(this);
	private ShowButton showBtn = new ShowButton(this);
	private QLabel placeholderLab = new QLabel(this);

}