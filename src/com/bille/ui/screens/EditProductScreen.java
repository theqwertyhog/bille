package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.Application;
import com.bille.Category;
import com.bille.Item;
import com.bille.Product;
import com.bille.Staff;
import com.bille.Unit;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextArea;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditProductScreen extends QDialog {

	private EditProductScreen(Product prd, QWidget parent) {
		super(parent);
		this.setWindowTitle("Edit product - " + prd.getName());
		this.prd = prd;

		initWidgets();
		initCatBox();
		initUnitBox();
		readFields();
		doSignals();
	}

	public static Product edit(Product p, QWidget parent) {
		EditProductScreen s = new EditProductScreen(p, parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.updatedPrd;
		}

		return null;
	}

	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateProduct()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void updateProduct() {
		Product p = new Product();

		try {
			p.setProductID(prd.getProductID());
			p.setName(nameBox.getText());
			p.setCode(codeBox.getText());
			p.setCategory(catList.get(catBox.currentIndex()).getCatID());
			p.setUnit(Unit.valueOf(unitBox.currentText()));
			p.setQuantity((float) quantBox.getVal());
			p.setPricePerUnit((float) priceBox.getVal());
			p.setAvailable(isAvailableChk.isChecked());
			
			int ret = p.update();
			
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
		
		this.updatedPrd = p;
		this.accept();
	}

	private void readFields() {
		nameBox.setText(prd.getName());
		codeBox.setText(prd.getCode());

		for (Category c : catList) {
			if (c.getCatID().equals(prd.getCategory())) {
				catBox.setCurrentIndex(catList.indexOf(c));
			}
		}

		for (Unit u : Unit.values()) {
			if (u.equals(prd.getUnit())) {
				unitBox.setCurrentIndex(u.ordinal());
			}
		}

		priceBox.setValue(prd.getPricePerUnit());
		quantBox.setValue(prd.getQuantity());

		if (prd.isAvailable()) {
			isAvailableChk.setChecked(true);
		}
	}

	private void initWidgets() {
		nameBox.setPlaceholder("Product name");
		codeBox.setPlaceholder("Product code");
		quantBox.setPlaceholder("Quantity available: ");
		priceBox.setPlaceholder("Price / unit: ");

		catBox.setEditable(false);

		mainLay.addWidget(nameBox, 0, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(codeBox, 0, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(catBox, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(unitBox, 1, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(priceBox, 2, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(isAvailableChk, 2, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(quantBox, 3, 0, 1, 3, new Alignment(
				AlignmentFlag.AlignCenter));

		mainLay.addWidget(cancelBtn, 4, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 4, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
	}

	private void initCatBox() {
		try {
			catList = Category.getAll();
		} catch (SQLException e) {
			Application.showSystemError(null);
			this.hide();
			this.disposeLater();
		}

		if (catList != null) {
			for (Category c : catList) {
				catBox.addItem(c.getName());
			}
		}
	}

	private void initUnitBox() {
		for (Unit u : Unit.values()) {
			unitBox.addItem(u.toString());
		}
	}

	private Product prd;
	private Product updatedPrd;

	private ArrayList<Category> catList;

	private QGridLayout mainLay = new QGridLayout(this);

	private SmartTextBox nameBox = new SmartTextBox(this);
	private SmartTextBox codeBox = new SmartTextBox(this);

	private QComboBox catBox = new QComboBox(this);
	private SmartDoubleBox quantBox = new SmartDoubleBox(this);
	private QComboBox unitBox = new QComboBox(this);

	private SmartDoubleBox priceBox = new SmartDoubleBox(this);
	private QCheckBox isAvailableChk = new QCheckBox("Is available?", this);

	private LongButton cancelBtn = new LongButton(this, "Cancel",
			GradientColor.RED);
	private LongButton updateBtn = new LongButton(this, "Update",
			GradientColor.GREEN);

}