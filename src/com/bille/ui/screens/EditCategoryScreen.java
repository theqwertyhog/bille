package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.Staff;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditCategoryScreen extends QDialog {
	
	private EditCategoryScreen(Category cat, QWidget parent) {
		super(parent);
		this.cat = cat;
		this.setWindowTitle("Edit category - " + cat.getName());
		
		initWidgets();
		readFields();
		doSignals();		
	}
	
	private void readFields() {
		catNameBox.setText(cat.getName());
	}
	
	public static Category edit(Category c, QWidget parent) {
		EditCategoryScreen s = new EditCategoryScreen(c, parent);
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		
		if (ok) {
			return s.updatedCat;
		}
		
		return null;
	}
	
	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateCat()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void updateCat() {
		Category c = new Category();

		try {
			c.setCatID(cat.getCatID());
			c.setName(catNameBox.getText());
			
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
		
		this.updatedCat = c;
		this.accept();
	}
	
	private void initWidgets() {
		catNameBox.setPlaceholder("Category name");
		
		mainLay.addWidget(catNameBox, 0, 0, 1, 3, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(cancelBtn, 1, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 1, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLay);
	}
	
	private Category cat;
	private Category updatedCat;
	
	private QGridLayout mainLay = new QGridLayout(this);
	private SmartTextBox catNameBox = new SmartTextBox(this);
	
	private LongButton cancelBtn = new LongButton(this, "Cancel", GradientColor.RED);
	private LongButton updateBtn = new LongButton(this, "Update", GradientColor.GREEN);

}