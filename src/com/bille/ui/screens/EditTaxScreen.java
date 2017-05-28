package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Staff;
import com.bille.Tax;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditTaxScreen extends QDialog {
	
	private EditTaxScreen(Tax tax, QWidget parent) {
		super(parent);
		this.setWindowTitle("Edit tax - " + tax.getName());
		this.tax = tax;
		
		initWidgets();
		readFields();
		doSignals();
	}
	
	public static Tax edit(Tax t, QWidget parent) {
		EditTaxScreen s = new EditTaxScreen(t, parent);
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		
		if (ok) {
			return s.updatedTax;
		}
		
		return null;
	}
	
	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateTax()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void updateTax() {
		Tax t = new Tax();

		try {
			t.setTaxID(tax.getTaxID());
			t.setName(nameBox.getText());
			t.setValue((float) valBox.getVal());
			
			if (isEnabledChk.isChecked()) {
				t.setEnabled(true);
			} else {
				t.setEnabled(false);
			}
			
			int ret = t.update();
			
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
		
		this.updatedTax = t;
		this.accept();
	}
	
	private void readFields() {
		nameBox.setText(tax.getName());
		valBox.setValue(tax.getValue());
		
		if (tax.isEnabled()) {
			isEnabledChk.setChecked(true);
		}
	}
	
	private void initWidgets() {
		nameBox.setPlaceholder("Name");
		valBox.setPlaceholder("Value: ");
		
		mainLay.addWidget(nameBox, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(valBox, 0, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(isEnabledChk, 1, 0, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(cancelBtn, 2, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 2, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLay);
	}
	
	private Tax tax;
	private Tax updatedTax;
	
	private QGridLayout mainLay = new QGridLayout(this);
	
	private SmartTextBox nameBox = new SmartTextBox(this);
	private SmartDoubleBox valBox = new SmartDoubleBox(this);
	private QCheckBox isEnabledChk = new QCheckBox("Is enabled?", this);
	
	private LongButton cancelBtn = new LongButton(this, "Cancel", GradientColor.RED);
	private LongButton updateBtn = new LongButton(this, "Update", GradientColor.GREEN);

}