package com.bille.widgets;

import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;

public class SmartTextArea extends QWidget {
	
	public SmartTextArea(QWidget parent) {
		super(parent);
		this.setObjectName("SmartTextBox");
		
		initWidgets();
		doSignals();
	}
	
	private void doSignals() {
		showBtn.clicked.connect(this, "showKeyboard()");
	}
	
	private void showKeyboard() {
		textArea.setPlainText(VirtualKeyboard.getText(this, textArea.toPlainText()));
	}
	
	public String getText() {
		return textArea.toPlainText();
	}
	
	public void setPlaceholder(String txt) {
		placeholderLab.setText(txt);
	}
	
	public void setText(String txt) {
		textArea.setPlainText(txt);
	}
	
	private void initWidgets() {
		textArea.setProperty("is_show", true);
		placeholderLab.setProperty("is_placeholder", true);
		
		mainLayout.addWidget(placeholderLab, 0, 0, new Alignment(AlignmentFlag.AlignLeft));
		mainLayout.addWidget(textArea, 1, 0);
		mainLayout.addWidget(showBtn, 1, 1);
		
		this.setLayout(mainLayout);
	}
	
	private QGridLayout mainLayout = new QGridLayout(this);
	private QPlainTextEdit textArea = new QPlainTextEdit(this);
	private ShowButton showBtn = new ShowButton(this);
	private QLabel placeholderLab = new QLabel(this);

}