package com.bille.widgets;

import com.bille.Application;
import com.bille.Store;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class Header extends QFrame {

	public Header(QWidget parent) {
		super(parent);
		this.setObjectName("Header");

		initLogo();
		initWidgets();
		doSignals();
	}

	private void doSignals() {
		closeBtn.clicked.connect(this, "exitApp()");
	}

	private void exitApp() {
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();

		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);

		QMessageBox.StandardButton ret = QMessageBox.warning(this, tr("Exit?"),
				tr("Are you sure you want to close this application?"),
				buttons, QMessageBox.StandardButton.No);

		if (ret.equals(QMessageBox.StandardButton.Yes)) {
			QApplication.exit(0);
		}
	}

	private void initLogo() {
		logoLab.setPixmap(new QPixmap("resources/logo.png"));
	}
	
	private void initRightWid() {
		rightLay.addWidget(clock);
		rightLay.addWidget(closeBtn);
		
		rightWid.setLayout(rightLay);
	}

	private void initWidgets() {
		initRightWid();
		
		storeNameLab.setText(store.getName());
		storeNameLab.setProperty("is_big_heading", true);

		
		closeBtn.setCursor(new QCursor(CursorShape.PointingHandCursor));
		closeBtn.setStyleSheet("background: none; "
				+ "background-image: url('resources/icons/close.png');"
				+ "background-repeat: no-repeat; background-position: center; ");

		mainLay.addWidget(logoLab, 0, new Alignment(AlignmentFlag.AlignLeft,
				AlignmentFlag.AlignTop));
		mainLay.addWidget(storeNameLab, 0, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(rightWid, 0, new Alignment(
				AlignmentFlag.AlignRight));
	}

	private Store store = Application.getInstance().getStore();

	private QHBoxLayout mainLay = new QHBoxLayout(this);
	private QLabel logoLab = new QLabel("Logo", this);
	private QLabel storeNameLab = new QLabel(this);

	private QWidget rightWid = new QWidget(this);
	private QHBoxLayout rightLay = new QHBoxLayout(rightWid);
	private DigitalClock clock = new DigitalClock(rightWid);
	private QPushButton closeBtn = new QPushButton(this);
}
