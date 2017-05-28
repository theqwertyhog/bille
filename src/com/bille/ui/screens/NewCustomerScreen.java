package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.UUID;

import com.bille.Application;
import com.bille.Customer;
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

public class NewCustomerScreen extends QDialog {
	
	private NewCustomerScreen(QWidget parent) {
		super(parent);
		this.setWindowTitle("New Customer");
		
		initWidgets();
		doSignals();
	}
	
	public static Customer createCustomer(QWidget parent) {
		NewCustomerScreen s = new NewCustomerScreen(parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.newCustomer;
		}

		return null;
	}
	
	private void doSignals() {
		createBtn.clicked.connect(this, "doCreate()");
		cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void doCreate() {
		newCustomer = new Customer();

		newCustomer.setCustomerID(UUID.randomUUID().toString());
		newCustomer.setFirstName(firstNameBox.getText());
		newCustomer.setLastName(lastNameBox.getText());
		newCustomer.setAge(ageBox.getVal());

		if (maleRad.isChecked()) {
			newCustomer.setSex('m');
		} else if (femaleRad.isChecked()) {
			newCustomer.setSex('f');
		} else if (otherRad.isChecked()) {
			newCustomer.setSex('o');
		}

		newCustomer.setPhoneNum(phNoBox.getText());

		try {
			int ret = newCustomer.createNew();
			if (ret != 0) {
				QMessageBox.critical(this, "Error", Customer.getErrMsg(ret));
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
		
		firstNameBox.setPlaceholder("First name");
		lastNameBox.setPlaceholder("Last name");
		ageBox.setPlaceholder("Age: ");
		phNoBox.setPlaceholder("Phone no.");
		
		mainLay.addWidget(firstNameBox, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(lastNameBox, 0, 1, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(ageBox, 1, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(sexWid, 1, 1, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(phNoBox, 2, 0, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
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
	
	private Customer newCustomer;
	
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
	
	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);
	private LongButton createBtn = new LongButton(this, "Create", GradientColor.GREEN);
	
}