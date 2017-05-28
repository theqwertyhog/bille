package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.UUID;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class NewCategoryScreen extends QDialog {
	
	private NewCategoryScreen(QWidget parent) {
		super(parent);
		this.setWindowTitle("New Category");
		
		initWidgets();
		doSignals();
	}
	
	public static Category createCat(QWidget parent) {
		NewCategoryScreen s = new NewCategoryScreen(parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.newCat;
		}

		return null;
	}
	
	private void initWidgets() {
		catNameBox.setPlaceholder("Category name");
		
		mainLay.addWidget(catNameBox, 0, 0, 1, 3, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(cancelBtn, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(createBtn, 1, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLay);
	}
	
	private void doSignals() {
		createBtn.clicked.connect(this, "doCreate()");
		cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void doCreate() {
		newCat = new Category();
		newCat.setCatID(UUID.randomUUID().toString());
		newCat.setName(catNameBox.getText());
		
		try {
			int ret = newCat.createNew();
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
	
	private Category newCat;
	
	private QGridLayout mainLay = new QGridLayout(this);
	private SmartTextBox catNameBox = new SmartTextBox(this);
	
	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);
	private LongButton createBtn = new LongButton(this, "Create", GradientColor.GREEN);

}
