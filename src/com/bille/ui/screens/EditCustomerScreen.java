package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Staff;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartNumberBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditCustomerScreen extends QDialog {
	
	private EditCustomerScreen(Customer cust, QWidget parent) {
		super(parent);
		this.setWindowTitle("Edit customer - " + cust.getFirstName() + " " + cust.getLastName());

		this.customer = cust;
		
		initWidgets();
		readFields();
		doSignals();
	}
	
	public static Customer edit(Customer c, QWidget parent) {
		EditCustomerScreen s = new EditCustomerScreen(c, parent);
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		
		if (ok) {
			return s.updatedCustomer;
		}
		
		return null;
	}
	
	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateCustomer()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void readFields() {
		firstNameBox.setText(customer.getFirstName());
		lastNameBox.setText(customer.getLastName());
		ageBox.setValue(customer.getAge());
		phNoBox.setText(customer.getPhoneNum());
		
		switch (customer.getSex()) {
		case 'm':
			maleRad.setChecked(true);
			break;
		case 'f':
			femaleRad.setChecked(true);
			break;
		case 'o':
			otherRad.setChecked(true);
		}
	}
	
	private void updateCustomer() {
		Customer c = new Customer();

		try {
			c.setCustomerID(customer.getCustomerID());
			c.setFirstName(firstNameBox.getText());
			c.setLastName(lastNameBox.getText());
			c.setAge(ageBox.getVal());
			
			if (maleRad.isChecked()) {
				c.setSex('m');
			}
			else if (femaleRad.isChecked()) {
				c.setSex('f');
			}
			else if (otherRad.isChecked()) {
				c.setSex('o');
			}
			
			c.setPhoneNum(phNoBox.getText());
			
			int ret = c.update();
			
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
		
		this.updatedCustomer = c;
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
		
		mainLay.addWidget(cancelBtn, 4, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 4, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLay);
	}
	
	private void initSexWid() {
		sexLay.addWidget(sexLab);
		sexLay.addWidget(maleRad);
		sexLay.addWidget(femaleRad);
		sexLay.addWidget(otherRad);
		
		sexWid.setLayout(sexLay);
	}
	
	private Customer customer;
	private Customer updatedCustomer;
	
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
	
	private LongButton cancelBtn = new LongButton(this, "Cancel", GradientColor.RED);
	private LongButton updateBtn = new LongButton(this, "Update", GradientColor.GREEN);
	
}