package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.UUID;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Tax;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class NewTaxScreen extends QDialog {
	
	private NewTaxScreen(QWidget parent) {
		super(parent);
		this.setWindowTitle("New Tax");
		
		initWidgets();
		doSignals();
	}
	
	public static Tax createTax(QWidget parent) {
		NewTaxScreen s = new NewTaxScreen(parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.newTax;
		}

		return null;
	}
	
	private void doSignals() {
		createBtn.clicked.connect(this, "doCreate()");
		cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void doCreate() {
		newTax = new Tax();

		newTax.setTaxID(UUID.randomUUID().toString());
		newTax.setName(nameBox.getText());
		newTax.setValue((float) valBox.getVal());
		
		if (isEnabledChk.isChecked()) {
			newTax.setEnabled(true);
		} else {
			newTax.setEnabled(false);
		}

		try {
			int ret = newTax.createNew();
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
		nameBox.setPlaceholder("Name");
		valBox.setPlaceholder("Value: ");
		
		mainLay.addWidget(nameBox, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(valBox, 0, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(isEnabledChk, 1, 0, 1, 3, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(cancelBtn, 2, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(createBtn, 2, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));		
		this.setLayout(mainLay);
	}
	
	private Tax newTax;
	
	private QGridLayout mainLay = new QGridLayout(this);
	
	private SmartTextBox nameBox = new SmartTextBox(this);
	private SmartDoubleBox valBox = new SmartDoubleBox(this);
	private QCheckBox isEnabledChk = new QCheckBox("Is enabled?", this);
	
	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);
	private LongButton createBtn = new LongButton(this, "Create", GradientColor.GREEN);

}
