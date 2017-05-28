package com.bille.widgets;

import com.bille.ui.GradientColor;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QLineEdit.EchoMode;

import java.util.ArrayList;

public class VirtualKeyboard extends QDialog {

	private VirtualKeyboard(QWidget parent) {
		super(parent);

		buildButtonsList();
		initWidgets();

		doSignals();
	}

	public static String getText(QWidget parent, String def) {
		VirtualKeyboard keyboard = new VirtualKeyboard(parent);
		keyboard.setWindowTitle("Virtual Keyboard");

		keyboard.sneakPeekBtn.setEnabled(false);
		keyboard.sneakPeekBtn.setProperty("is_disabled", true);

		boolean ok = keyboard.exec() == QDialog.DialogCode.Accepted.value();
		if (ok) {
			return keyboard.prompt.text();
		}
		return def;
	}

	public static String getPassword(QWidget parent, String def) {
		VirtualKeyboard keyboard = new VirtualKeyboard(parent);
		keyboard.setWindowTitle("Virtual Keyboard | Password");

		keyboard.prompt.setEchoMode(EchoMode.Password);

		boolean ok = keyboard.exec() == QDialog.DialogCode.Accepted.value();
		if (ok) {
			return keyboard.prompt.text();
		}
		return def;
	}

	private void doSignals() {
		this.okBtn.clicked.connect(this, "accept()");
		this.cancelBtn.clicked.connect(this, "reject()");

		capsBtn.clicked.connect(this, "toggleCaps()");

		for (MediumButton b : buttons) {
			b.clickedUpon.connect(this, "updatePrompt(String)");
		}

		spaceBtn.clickedUpon.connect(this, "updatePrompt(String)");
		clearBtn.clicked.connect(prompt, "clear()");
		sneakPeekBtn.pressed.connect(this, "sneakPeek()");
		sneakPeekBtn.released.connect(this, "hidePassword()");
		deleteBtn.clicked.connect(this, "delete()");

		okBtn.clicked.connect(this, "accept()");
		cancelBtn.clicked.connect(this, "reject()");
	}

	private void sneakPeek() {
		prompt.setEchoMode(EchoMode.Normal);
	}

	private void hidePassword() {
		prompt.setEchoMode(EchoMode.Password);
	}

	private void delete() {
		String txt = prompt.text();

		if (txt.length() > 0) {
			txt = txt.substring(0, txt.length() - 1);
		}
		prompt.setText(txt);
	}

	private void updatePrompt(String s) {
		prompt.setText(prompt.text() + s);
	}

	private void toggleCaps() {
		if (isCapsOn) {
			isCapsOn = false;
			capsBtn.setStyleSheet("color: white;");
			for (MediumButton b : buttons) {
				b.setText(b.text().toLowerCase());
			}
		} else {
			isCapsOn = true;
			capsBtn.setStyleSheet("color: brown;");
			for (MediumButton b : buttons) {
				b.setText(b.text().toUpperCase());
			}
		}
	}

	private void buildButtonsList() {
		buttons.add(aBtn);
		buttons.add(bBtn);
		buttons.add(cBtn);
		buttons.add(dBtn);
		buttons.add(eBtn);
		buttons.add(fBtn);
		buttons.add(gBtn);
		buttons.add(hBtn);
		buttons.add(iBtn);
		buttons.add(jBtn);
		buttons.add(kBtn);
		buttons.add(lBtn);
		buttons.add(mBtn);
		buttons.add(nBtn);
		buttons.add(oBtn);
		buttons.add(pBtn);
		buttons.add(qBtn);
		buttons.add(rBtn);
		buttons.add(sBtn);
		buttons.add(tBtn);
		buttons.add(uBtn);
		buttons.add(vBtn);
		buttons.add(wBtn);
		buttons.add(xBtn);
		buttons.add(yBtn);
		buttons.add(zBtn);
	}

	private void initWidgets() {
		initBottomWidget();
		initLowerWidget();
		initMidWidget();

		prompt.setEnabled(false);
		prompt.setText("");

		mainLayout.addWidget(prompt);
		mainLayout.addWidget(midWidget, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLayout.addWidget(spaceBtn, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLayout.addWidget(lowerWidget, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLayout.addWidget(bottomWidget, 0, new Alignment(
				AlignmentFlag.AlignCenter, AlignmentFlag.AlignTop));

		this.setLayout(mainLayout);
	}

	private void initMidWidget() {
		midLayout.addWidget(qBtn, 0, 0);
		midLayout.addWidget(wBtn, 0, 1);
		midLayout.addWidget(eBtn, 0, 2);
		midLayout.addWidget(rBtn, 0, 3);
		midLayout.addWidget(tBtn, 0, 4);
		midLayout.addWidget(yBtn, 0, 5);
		midLayout.addWidget(uBtn, 0, 6);
		midLayout.addWidget(iBtn, 0, 7);
		midLayout.addWidget(oBtn, 0, 8);
		midLayout.addWidget(pBtn, 0, 9);

		midLayout.addWidget(aBtn, 1, 0);
		midLayout.addWidget(sBtn, 1, 1);
		midLayout.addWidget(dBtn, 1, 2);
		midLayout.addWidget(fBtn, 1, 3);
		midLayout.addWidget(gBtn, 1, 4);
		midLayout.addWidget(hBtn, 1, 5);
		midLayout.addWidget(jBtn, 1, 6);
		midLayout.addWidget(kBtn, 1, 7);
		midLayout.addWidget(lBtn, 1, 8);
		midLayout.addWidget(dashBtn, 1, 9);

		midLayout.addWidget(capsBtn, 2, 0);
		midLayout.addWidget(zBtn, 2, 1);
		midLayout.addWidget(xBtn, 2, 2);
		midLayout.addWidget(cBtn, 2, 3);
		midLayout.addWidget(vBtn, 2, 4);
		midLayout.addWidget(bBtn, 2, 5);
		midLayout.addWidget(nBtn, 2, 6);
		midLayout.addWidget(mBtn, 2, 7);
		midLayout.addWidget(sneakPeekBtn, 2, 8);
		midLayout.addWidget(deleteBtn, 2, 9);

		midWidget.setLayout(midLayout);
	}

	private void initLowerWidget() {
		lowerLayout.addWidget(spaceBtn);

		lowerWidget.setLayout(lowerLayout);
	}

	private void initBottomWidget() {
		bottomLayout.addWidget(cancelBtn);
		bottomLayout.addWidget(clearBtn);
		bottomLayout.addWidget(okBtn);

		bottomWidget.setLayout(bottomLayout);
	}

	private ArrayList<MediumButton> buttons = new ArrayList<MediumButton>();
	private boolean isCapsOn;
	private boolean isPassword;

	private QVBoxLayout mainLayout = new QVBoxLayout(this);
	private QLineEdit prompt = new QLineEdit(this);

	private QWidget midWidget = new QWidget(this);
	private QGridLayout midLayout = new QGridLayout(midWidget);

	// The buttons
	private MediumButton aBtn = new MediumButton(midWidget, "a",
			GradientColor.GREY);
	private MediumButton bBtn = new MediumButton(midWidget, "b",
			GradientColor.GREY);
	private MediumButton cBtn = new MediumButton(midWidget, "c",
			GradientColor.GREY);
	private MediumButton dBtn = new MediumButton(midWidget, "d",
			GradientColor.GREY);
	private MediumButton eBtn = new MediumButton(midWidget, "e",
			GradientColor.GREY);
	private MediumButton fBtn = new MediumButton(midWidget, "f",
			GradientColor.GREY);
	private MediumButton gBtn = new MediumButton(midWidget, "g",
			GradientColor.GREY);
	private MediumButton hBtn = new MediumButton(midWidget, "h",
			GradientColor.GREY);
	private MediumButton iBtn = new MediumButton(midWidget, "i",
			GradientColor.GREY);
	private MediumButton jBtn = new MediumButton(midWidget, "j",
			GradientColor.GREY);
	private MediumButton kBtn = new MediumButton(midWidget, "k",
			GradientColor.GREY);
	private MediumButton lBtn = new MediumButton(midWidget, "l",
			GradientColor.GREY);
	private MediumButton mBtn = new MediumButton(midWidget, "m",
			GradientColor.GREY);
	private MediumButton nBtn = new MediumButton(midWidget, "n",
			GradientColor.GREY);
	private MediumButton oBtn = new MediumButton(midWidget, "o",
			GradientColor.GREY);
	private MediumButton pBtn = new MediumButton(midWidget, "p",
			GradientColor.GREY);
	private MediumButton qBtn = new MediumButton(midWidget, "q",
			GradientColor.GREY);
	private MediumButton rBtn = new MediumButton(midWidget, "r",
			GradientColor.GREY);
	private MediumButton sBtn = new MediumButton(midWidget, "s",
			GradientColor.GREY);
	private MediumButton tBtn = new MediumButton(midWidget, "t",
			GradientColor.GREY);
	private MediumButton uBtn = new MediumButton(midWidget, "u",
			GradientColor.GREY);
	private MediumButton vBtn = new MediumButton(midWidget, "v",
			GradientColor.GREY);
	private MediumButton wBtn = new MediumButton(midWidget, "w",
			GradientColor.GREY);
	private MediumButton xBtn = new MediumButton(midWidget, "x",
			GradientColor.GREY);
	private MediumButton yBtn = new MediumButton(midWidget, "y",
			GradientColor.GREY);
	private MediumButton zBtn = new MediumButton(midWidget, "z",
			GradientColor.GREY);

	private MediumButton capsBtn = new MediumButton(midWidget, "CAPS",
			GradientColor.BLUE);
	private MediumButton dashBtn = new MediumButton(midWidget, "-",
			GradientColor.GREY);
	private MediumButton sneakPeekBtn = new MediumButton(midWidget, "SP",
			GradientColor.BLUE);
	private MediumButton deleteBtn = new MediumButton(midWidget, "Del",
			GradientColor.RED);

	private QWidget lowerWidget = new QWidget(this);
	private QHBoxLayout lowerLayout = new QHBoxLayout(lowerWidget);
	private LongButton spaceBtn = new LongButton(lowerWidget, " ",
			GradientColor.GREY);

	private QWidget bottomWidget = new QWidget(this);
	private QHBoxLayout bottomLayout = new QHBoxLayout(bottomWidget);
	private RectButton cancelBtn = new RectButton(bottomWidget, "Cancel",
			GradientColor.RED);
	private RectButton clearBtn = new RectButton(bottomWidget, "Clear",
			GradientColor.BLUE);
	private RectButton okBtn = new RectButton(bottomWidget, "OK",
			GradientColor.GREEN);

}
