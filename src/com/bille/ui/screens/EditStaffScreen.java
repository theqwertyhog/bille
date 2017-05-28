package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Staff;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartNumberBox;
import com.bille.widgets.SmartTextArea;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditStaffScreen extends QDialog {
	
	private EditStaffScreen(Staff staff, QWidget parent) {
		super(parent);
		this.setWindowTitle("Edit staff - " + staff.getFirstName() + " " + staff.getLastName());

		this.staff = staff;

		initWidgets();
		readFields();
		doSignals();
	}
	
	public static Staff edit(Staff staff, QWidget parent) {
		EditStaffScreen s = new EditStaffScreen(staff, parent);
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		
		if (ok) {
			return s.updatedStaff;
		}
		
		return null;
	}
	
	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateStaff()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void updateStaff() {
		Staff s = new Staff();

		try {
			s.setStaffID(staff.getStaffID());
			s.setFirstName(firstNameBox.getText());
			s.setLastName(lastNameBox.getText());
			s.setAge(ageBox.getVal());
			
			if (maleRad.isChecked()) {
				s.setSex('m');
			}
			else if (femaleRad.isChecked()) {
				s.setSex('f');
			}
			else if (otherRad.isChecked()) {
				s.setSex('o');
			}
			
			s.setPhoneNum(phNoBox.getText());
			s.setAddress(addressBox.getText());
			s.setUserID(userIDBox.getText());
			
			if (staffRad.isChecked()) {
				s.setRole(UserRole.STAFF);
			}
			else if (adminRad.isChecked()) {
				s.setRole(UserRole.ADMIN);
			}
			
			if (isActiveChk.isChecked()) {
				s.setActive(true);
			} else {
				s.setActive(false);
			}
			
			int ret = s.update();
			
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
		
		this.updatedStaff = s;
		this.accept();
	}
	
	private void readFields() {
		firstNameBox.setText(staff.getFirstName());
		lastNameBox.setText(staff.getLastName());
		ageBox.setValue(staff.getAge());
		phNoBox.setText(staff.getPhoneNum());
		userIDBox.setText(staff.getUserID());
		addressBox.setText(staff.getAddress());
		
		switch (staff.getSex()) {
		case 'm':
			maleRad.setChecked(true);
			break;
		case 'f':
			femaleRad.setChecked(true);
			break;
		case 'o':
			otherRad.setChecked(true);
		}
		
		if (staff.getRole() == UserRole.ADMIN) {
			adminRad.setChecked(true);
		} else if (staff.getRole() == UserRole.STAFF) {
			staffRad.setChecked(true);
		}
		
		if (staff.isActive()) {
			isActiveChk.setChecked(true);
		}
	}
	
	private void initWidgets() {
		initSexWid();
		initUserRoleWid();
		
		firstNameBox.setPlaceholder("First name");
		lastNameBox.setPlaceholder("Last name");
		ageBox.setPlaceholder("Age: ");
		phNoBox.setPlaceholder("Phone no.");
		userIDBox.setPlaceholder("User ID");
		addressBox.setPlaceholder("Address");
		
		mainLay.addWidget(firstNameBox, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(lastNameBox, 0, 1, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(ageBox, 1, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(sexWid, 1, 1, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(phNoBox, 2, 0, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(addressBox, 3, 0, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(userIDBox, 4, 0, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(userRoleWid, 5, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(isActiveChk, 5, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(cancelBtn, 6, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 6, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
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
	
	private Staff staff;
	private Staff updatedStaff;
	
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
	
	private QWidget userRoleWid = new QWidget(this);
	private QHBoxLayout userRoleLay = new QHBoxLayout(userRoleWid);
	private QLabel userRoleLab = new QLabel("Role: ", userRoleWid);
	private QRadioButton staffRad = new QRadioButton("Staff", userRoleWid);
	private QRadioButton adminRad = new QRadioButton("Admin", userRoleWid);
	
	private QCheckBox isActiveChk = new QCheckBox("Is active?", this);
	
	private LongButton updateBtn = new LongButton(this, "Update", GradientColor.GREEN);
	private LongButton cancelBtn = new LongButton(this, "Cancel", GradientColor.RED);
	
}