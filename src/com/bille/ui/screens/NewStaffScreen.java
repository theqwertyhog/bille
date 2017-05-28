package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.UUID;

import com.bille.Application;
import com.bille.Staff;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartNumberBox;
import com.bille.widgets.SmartPasswordBox;
import com.bille.widgets.SmartTextArea;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class NewStaffScreen extends QDialog {

	private NewStaffScreen(QWidget parent) {
		super(parent);
		this.setWindowTitle("New Staff");

		initWidgets();
		doSignals();
	}

	private void doSignals() {
		createBtn.clicked.connect(this, "doCreate()");
		cancelBtn.clicked.connect(this, "reject()");
	}

	public static Staff createStaff(QWidget parent) {
		NewStaffScreen s = new NewStaffScreen(parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.newStaff;
		}

		return null;
	}

	private void doCreate() {
		newStaff = new Staff();

		newStaff.setStaffID(UUID.randomUUID().toString());
		newStaff.setFirstName(firstNameBox.getText());
		newStaff.setLastName(lastNameBox.getText());
		newStaff.setAge(ageBox.getVal());

		if (maleRad.isChecked()) {
			newStaff.setSex('m');
		} else if (femaleRad.isChecked()) {
			newStaff.setSex('f');
		} else if (otherRad.isChecked()) {
			newStaff.setSex('o');
		}

		newStaff.setPhoneNum(phNoBox.getText());
		newStaff.setAddress(addressBox.getText());
		newStaff.setUserID(userIDBox.getText());

		if (staffRad.isChecked()) {
			newStaff.setRole(UserRole.STAFF);
		} else if (adminRad.isChecked()) {
			newStaff.setRole(UserRole.ADMIN);
		}

		if (isActiveChk.isChecked()) {
			newStaff.setActive(true);
		} else {
			newStaff.setActive(false);
		}
		
		newStaff.setPassword(passwordBox.getPassword());

		try {
			int ret = newStaff.createNew();
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

	private void initWidgets() {
		initSexWid();
		initUserRoleWid();

		firstNameBox.setPlaceholder("First name");
		lastNameBox.setPlaceholder("Last name");
		ageBox.setPlaceholder("Age: ");
		phNoBox.setPlaceholder("Phone no.");
		userIDBox.setPlaceholder("User ID");
		passwordBox.setPlaceholder("Password");
		addressBox.setPlaceholder("Address");

		mainLay.addWidget(firstNameBox, 0, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(lastNameBox, 0, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(ageBox, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(sexWid, 1, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(phNoBox, 2, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(addressBox, 3, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(userIDBox, 4, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(passwordBox, 4, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(userRoleWid, 5, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(isActiveChk, 5, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(cancelBtn, 6, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(createBtn, 6, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		this.setLayout(mainLay);
	}

	private void initSexWid() {
		sexLay.addWidget(sexLab);
		sexLay.addWidget(maleRad);
		sexLay.addWidget(femaleRad);
		sexLay.addWidget(otherRad);

		sexWid.setLayout(sexLay);
	}

	private void initUserRoleWid() {
		userRoleLay.addWidget(userRoleLab);
		userRoleLay.addWidget(staffRad);
		userRoleLay.addWidget(adminRad);

		userRoleWid.setLayout(userRoleLay);
	}

	private Staff newStaff;

	private QGridLayout mainLay = new QGridLayout(this);

	private SmartTextBox firstNameBox = new SmartTextBox(this);
	private SmartTextBox lastNameBox = new SmartTextBox(this);

	private SmartNumberBox ageBox = new SmartNumberBox(this);

	private QWidget sexWid = new QWidget(this);
	private QHBoxLayout sexLay = new QHBoxLayout(sexWid);
	private QLabel sexLab = new QLabel("Sex: ", sexWid);
	private QRadioButton maleRad = new QRadioButton("Male", sexWid);
	private QRadioButton femaleRad = new QRadioButton("Female", sexWid);
	private QRadioButton otherRad = new QRadioButton("Other", sexWid);

	private SmartTextBox phNoBox = new SmartTextBox(this);
	private SmartTextArea addressBox = new SmartTextArea(this);

	private SmartTextBox userIDBox = new SmartTextBox(this);
	private SmartPasswordBox passwordBox = new SmartPasswordBox(this);

	private QWidget userRoleWid = new QWidget(this);
	private QHBoxLayout userRoleLay = new QHBoxLayout(userRoleWid);
	private QLabel userRoleLab = new QLabel("Role: ", userRoleWid);
	private QRadioButton staffRad = new QRadioButton("Staff", userRoleWid);
	private QRadioButton adminRad = new QRadioButton("Admin", userRoleWid);

	private QCheckBox isActiveChk = new QCheckBox("Is active?", this);

	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);
	private LongButton createBtn = new LongButton(this, "Create",
			GradientColor.GREEN);

}
