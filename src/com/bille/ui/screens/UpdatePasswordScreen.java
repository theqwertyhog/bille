package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Staff;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartPasswordBox;
import com.trolltech.qt.core.QTimer;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class UpdatePasswordScreen extends QDialog {

	private UpdatePasswordScreen(String userID, QWidget parent) {
		super(parent);
		this.setWindowTitle("Change password - " + userID);
		this.userID = userID;

		initWidgets();
		doSignals();
	}

	private void doSignals() {
		updateBtn.clicked.connect(this, "doUpdate()");
		cancelBtn.clicked.connect(this, "reject()");
		showPasswordBtn.clicked.connect(this, "showPassword()");
		sneakPeekTimer.timeout.connect(this, "sneakPeekTimeout()");
	}

	private void showPassword() {
		showPasswordBtn.setEnabled(false);
		try {
			String pass = Staff.readPassword(userID);
			showPasswordBtn.setText(pass);
			sneakPeekTimer.start();
		} catch (NoSuchStaffIDException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}

	}

	private void sneakPeekTimeout() {
		showPasswordBtn.setText("Show password");
		showPasswordBtn.setEnabled(true);
	}

	private void initSneakPeekWid() {
		showPasswordBtn.setCursor(new QCursor(CursorShape.PointingHandCursor));
		sneakPeekTimer.setInterval(3000);
		sneakPeekTimer.setSingleShot(true);

		sneakPeekLay.addWidget(showPasswordBtn);
		sneakPeekWid.setLayout(sneakPeekLay);
	}

	private void initWidgets() {
		initSneakPeekWid();

		oldPassBox.setPlaceholder("Old password");
		newPassBox.setPlaceholder("New password");
		confirmPassBox.setPlaceholder("Confirm password");

		mainLay.addWidget(sneakPeekWid);
		mainLay.addWidget(oldPassBox);
		mainLay.addWidget(newPassBox);
		mainLay.addWidget(confirmPassBox);

		QWidget btnWid = new QWidget(this);
		QHBoxLayout btnLay = new QHBoxLayout(btnWid);
		btnLay.addWidget(cancelBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		btnLay.addWidget(updateBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		btnWid.setLayout(btnLay);

		mainLay.addWidget(btnWid);

		this.setLayout(mainLay);
	}

	public static boolean updatePassword(String userID, QWidget parent) {
		UpdatePasswordScreen s = new UpdatePasswordScreen(userID, parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return true;
		}

		return false;
	}

	private void doUpdate() {

		if (!newPassBox.getPassword().equals(confirmPassBox.getPassword())) {
			QMessageBox.critical(this, "Error",
					"New password and confirm password don't match");
			return;
		}

		try {
			int ret = Staff.updatePassword(userID, oldPassBox.getPassword(),
					newPassBox.getPassword());

			if (ret != 0) {
				QMessageBox.critical(this, "Error", Staff.getErrMsg(ret));
				return;
			}

		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}

		this.accept();
	}

	private String userID;

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QWidget sneakPeekWid = new QWidget(this);
	private QHBoxLayout sneakPeekLay = new QHBoxLayout(sneakPeekWid);
	private LongButton showPasswordBtn = new LongButton(sneakPeekWid,
			"Show password", GradientColor.TRANSPARENT);
	private QTimer sneakPeekTimer = new QTimer();

	private SmartPasswordBox oldPassBox = new SmartPasswordBox(this);
	private SmartPasswordBox newPassBox = new SmartPasswordBox(this);
	private SmartPasswordBox confirmPassBox = new SmartPasswordBox(this);

	private LongButton updateBtn = new LongButton(this, "Update",
			GradientColor.GREEN);
	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);

}
