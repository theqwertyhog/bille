package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.UUID;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.Item;
import com.bille.Staff;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextArea;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class EditItemScreen extends QDialog {
	
	private EditItemScreen(Item item, QWidget parent) {
		super(parent);
		this.setWindowTitle("Edit item - " + item.getName());
		this.item = item;
		
		initWidgets();
		initCatBox();
		readFields();
		doSignals();
	}
	
	public static Item edit(Item i, QWidget parent) {
		EditItemScreen s = new EditItemScreen(i, parent);
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		
		if (ok) {
			return s.updatedItem;
		}
		
		return null;
	}
	
	private void doSignals() {
		this.updateBtn.clicked.connect(this, "updateItem()");
		this.cancelBtn.clicked.connect(this, "reject()");
	}
	
	private void updateItem() {
		Item i = new Item();

		try {
			i.setItemID(item.getItemID());
			i.setName(nameBox.getText());
			i.setCode(codeBox.getText());
			i.setCategory(catList.get(catBox.currentIndex()).getCatID());
			i.setDesc(descBox.getText());
			i.setPrice((float) priceBox.getVal());
			i.setAvailable(isAvailableChk.isChecked());
			
			int ret = i.update();
			
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
		
		this.updatedItem = i;
		this.accept();
	}
	
	private void readFields() {
		nameBox.setText(item.getName());
		codeBox.setText(item.getCode());
		descBox.setText(item.getDesc());
		priceBox.setValue(item.getPrice());
		
		for(Category c : catList) {
			if (c.getCatID().equals(item.getCategory())) {
				catBox.setCurrentIndex(catList.indexOf(c));
			}
		}
		
		if (item.isAvailable()) {
			isAvailableChk.setChecked(true);
		}
	}
	
	private void initWidgets() {
		nameBox.setPlaceholder("Item name");
		codeBox.setPlaceholder("Item code");
		descBox.setPlaceholder("Description");
		priceBox.setPlaceholder("Price: ");
		
		catBox.setEditable(false);
		
		mainLay.addWidget(nameBox, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(codeBox, 0, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(catBox, 1, 0, 1, 3, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(descBox, 2, 0, 1, 3, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(priceBox, 3, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(isAvailableChk, 3, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		mainLay.addWidget(cancelBtn, 4, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(updateBtn, 4, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
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
		
	private Item item;
	private Item updatedItem;
	
	private ArrayList<Category> catList;
	
	private QGridLayout mainLay = new QGridLayout(this);
	
	private SmartTextBox nameBox = new SmartTextBox(this);
	private SmartTextBox codeBox = new SmartTextBox(this);
	
	private QComboBox catBox = new QComboBox(this);
	private SmartTextArea descBox = new SmartTextArea(this);
	
	private SmartDoubleBox priceBox = new SmartDoubleBox(this);
	private QCheckBox isAvailableChk = new QCheckBox("Is available?", this);
	
	private LongButton cancelBtn = new LongButton(this, "Cancel", GradientColor.RED);
	private LongButton updateBtn = new LongButton(this, "Update", GradientColor.GREEN);

}