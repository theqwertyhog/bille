package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.Item;
import com.bille.Product;
import com.bille.Unit;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextArea;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class NewProductScreen extends QDialog {

	private NewProductScreen(QWidget parent) {
		super(parent);
		this.setWindowTitle("New Product");

		initWidgets();
		initCatBox();
		initUnitBox();
		doSignals();
	}
	
	public static Product createProduct(QWidget parent) {
		NewProductScreen s = new NewProductScreen(parent);

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			return s.newProd;
		}

		return null;
	}
	
	private void doSignals() {
		createBtn.clicked.connect(this, "doCreate()");
		cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void doCreate() {
		newProd = new Product();
		
		newProd.setProductID(UUID.randomUUID().toString());
		newProd.setName(nameBox.getText());
		newProd.setCode(codeBox.getText());
		newProd.setCategory(catList.get(catBox.currentIndex()).getCatID());
		newProd.setQuantity((float) quantBox.getVal());
		newProd.setPricePerUnit((float) priceBox.getVal());
		newProd.setUnit(Unit.valueOf(unitBox.currentText()));
		newProd.setAvailable(isAvailableChk.isChecked());
		
		try {
			int ret = newProd.createNew();
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
		mainLay.addWidget(createBtn, 4, 1, 1, 2, new Alignment(
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

	private Product newProd;
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
	private LongButton createBtn = new LongButton(this, "Create",
			GradientColor.GREEN);

}