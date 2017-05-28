package com.bille.widgets;

import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.core.Qt.FocusPolicy;
import com.trolltech.qt.core.Qt.FocusReason;
import com.trolltech.qt.gui.*;

public class SmartTextBox extends QWidget {
	
	public SmartTextBox(QWidget parent) {
		super(parent);
		this.setObjectName("SmartTextBox");
		
		initWidgets();
		doSignals();		
	}
	
	private void doSignals() {
		showBtn.clicked.connect(this, "showKeyboard()");
	}
	
	private void showKeyboard() {
		textBox.setText(VirtualKeyboard.getText(this, textBox.text()));
	}
	
	public void setPlaceholder(String text) {
		placeholderLab.setText(text);
	}
	
	public String getText() {
		return textBox.text();
	}
	
	public void setText(String txt) {
		textBox.setText(txt);
		textBox.setCursorPosition(0);
	}
	
	private void initWidgets() {
		textBox.setProperty("is_show", true);
		placeholderLab.setProperty("is_placeholder", true);
		
		mainLayout.addWidget(placeholderLab, 0, 0, new Alignment(AlignmentFlag.AlignLeft));
		mainLayout.addWidget(textBox, 1, 0);
		mainLayout.addWidget(showBtn, 1, 1);
		
		this.setLayout(mainLayout);
	}
	
	public void setKeyboardFocus() {
		textBox.setFocus();
	}
	
	private QGridLayout mainLayout = new QGridLayout(this);
	private QLineEdit textBox = new QLineEdit(this);
	private ShowButton showBtn = new ShowButton(this);
	private QLabel placeholderLab = new QLabel(this);

	public Signal0 returnPressed = textBox.returnPressed;
}
