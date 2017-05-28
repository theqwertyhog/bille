package com.bille.widgets;

import com.bille.ui.GradientColor;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class VirtualNumpad extends QDialog {

	// Numpad modes
	public static final int INTEGER = 1, DECIMAL = 2;

	private VirtualNumpad(QWidget parent, int mode) {
		super(parent);
		this.setObjectName("VirtualNumpad");

		this.mode = mode;
		
		initWidgets();
		doSignals();
	}
	
	public static int getInt(QWidget parent, int mode, int def) {
		VirtualNumpad numpad = new VirtualNumpad(parent, mode);
		numpad.setWindowTitle("Virtual Numpad");
		
		boolean ok = numpad.exec() == QDialog.DialogCode.Accepted.value();
		if (ok) {
			return Integer.parseInt(numpad.prompt.text());
		}
		return def;
	}
	
	public static double getDouble(QWidget parent, int mode, double def) {
		VirtualNumpad numpad = new VirtualNumpad(parent, mode);
		numpad.setWindowTitle("Virtual Numpad");
		
		boolean ok = numpad.exec() == QDialog.DialogCode.Accepted.value();
		if (ok) {
			return Double.parseDouble(numpad.prompt.text());
		}
		return def;
	}
	
	private void doSignals() {
		okBtn.clicked.connect(this, "accept()");
		cancelBtn.clicked.connect(this, "reject()");
		
		clearBtn.clicked.connect(this, "clearPrompt()");
		
		oneBtn.clickedUpon.connect(this, "updateInput(String)");
		twoBtn.clickedUpon.connect(this, "updateInput(String)");
		threeBtn.clickedUpon.connect(this, "updateInput(String)");
		fourBtn.clickedUpon.connect(this, "updateInput(String)");
		fiveBtn.clickedUpon.connect(this, "updateInput(String)");
		sixBtn.clickedUpon.connect(this, "updateInput(String)");
		sevenBtn.clickedUpon.connect(this, "updateInput(String)");
		eightBtn.clickedUpon.connect(this, "updateInput(String)");
		nineBtn.clickedUpon.connect(this, "updateInput(String)");
		zeroBtn.clickedUpon.connect(this, "updateInput(String)");
		dotBtn.clickedUpon.connect(this, "updateInput(String)");
	}
	
	private void clearPrompt() {
		prompt.setText("");
		hasDot = false;
	}
	
	private void updateInput(String s) {
		if (s.equals(".")) {
			if (hasDot) {
				return;
			}
			else {
				if (prompt.text().equals("")) {
					prompt.setText("0");
				}
				prompt.setText(prompt.text() + s);
				hasDot = true;
				return;
			}
			
		}
		prompt.setText(prompt.text() + s);
	}
		
	private void initWidgets() {
		initMidWidget();
		initBottomWidget();
		
		prompt.setProperty("is_prompt", true);
		prompt.setEnabled(false);
		prompt.setText("");
		
		mainLayout.addWidget(prompt);
		mainLayout.addWidget(midWidget, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLayout.addWidget(bottomWidget, 0, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLayout);
	}
	
	private void initMidWidget() {
		
		if (this.mode == INTEGER) {
			dotBtn.setEnabled(false);
			dotBtn.setProperty("is_disabled", true);
		}
		
		midLayout.addWidget(oneBtn, 0, 0);
		midLayout.addWidget(twoBtn, 0, 1);
		midLayout.addWidget(threeBtn, 0, 2);
		midLayout.addWidget(fourBtn, 1, 0);
		midLayout.addWidget(fiveBtn, 1, 1);
		midLayout.addWidget(sixBtn, 1, 2);
		midLayout.addWidget(sevenBtn, 2, 0);
		midLayout.addWidget(eightBtn, 2, 1);
		midLayout.addWidget(nineBtn, 2, 2);
		midLayout.addWidget(dotBtn, 3, 0, new Alignment(AlignmentFlag.AlignCenter));
		midLayout.addWidget(zeroBtn, 3, 1, new Alignment(AlignmentFlag.AlignCenter));
		
		midWidget.setLayout(midLayout);
	}
	
	private void initBottomWidget() {
		bottomLayout.addWidget(cancelBtn);
		bottomLayout.addWidget(clearBtn);
		bottomLayout.addWidget(okBtn);
		
		bottomWidget.setLayout(bottomLayout);
	}

	private int mode;
	private boolean hasDot = false;;

	private QVBoxLayout mainLayout = new QVBoxLayout(this);
	private QLineEdit prompt = new QLineEdit(this);

	private QWidget midWidget = new QWidget(this);
	private QGridLayout midLayout = new QGridLayout(midWidget);

	// The buttons
	private LargeButton oneBtn = new LargeButton(midWidget, "1", GradientColor.GREY);
	private LargeButton twoBtn = new LargeButton(midWidget, "2", GradientColor.GREY);
	private LargeButton threeBtn = new LargeButton(midWidget, "3", GradientColor.GREY);
	private LargeButton fourBtn = new LargeButton(midWidget, "4", GradientColor.GREY);
	private LargeButton fiveBtn = new LargeButton(midWidget, "5", GradientColor.GREY);
	private LargeButton sixBtn = new LargeButton(midWidget, "6", GradientColor.GREY);
	private LargeButton sevenBtn = new LargeButton(midWidget, "7", GradientColor.GREY);
	private LargeButton eightBtn = new LargeButton(midWidget, "8", GradientColor.GREY);
	private LargeButton nineBtn = new LargeButton(midWidget, "9", GradientColor.GREY);
	private LargeButton zeroBtn = new LargeButton(midWidget, "0", GradientColor.GREY);
	private LargeButton dotBtn = new LargeButton(midWidget, ".", GradientColor.GREY);
	
	private QWidget bottomWidget = new QWidget(this);
	private QHBoxLayout bottomLayout = new QHBoxLayout(bottomWidget);
	private RectButton cancelBtn = new RectButton(bottomWidget, "Cancel", GradientColor.RED);
	private RectButton clearBtn = new RectButton(bottomWidget, "Clear", GradientColor.BLUE);
	private RectButton okBtn = new RectButton(bottomWidget, "OK", GradientColor.GREEN);

}
