package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.UUID;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Product;
import com.bille.RetailOrder;
import com.bille.Staff;
import com.bille.Unit;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartNumberBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.ScrollMode;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QLayout.SizeConstraint;
import com.trolltech.qt.gui.QSizePolicy.Policy;

public class RetailNewOrderScreen extends QWidget {

	public RetailNewOrderScreen(QWidget parent) {
		super(parent);

		initProductsWid();
		initWidgets();
		doSignals();

		productCodeBox.setKeyboardFocus();
	}

	private void doSignals() {
		productNameBox.returnPressed.connect(this, "searchByName()");
		addBtn.clicked.connect(this, "addProduct()");
		proceedBtn.clicked.connect(this, "placeOrder()");
		customerBox.returnPressed.connect(this, "assignCustomer()");
		delBtn.clicked.connect(this, "removeProducts()");

		productCodeBox.returnPressed.connect(this, "addProduct()");
		nextPrevBtn.clicked.connect(this, "nextPrev()");
	}

	// Control functions
	
	private void nextPrev() {
		if (currentPage == 1) {
			if (order.getProducts().size() <= 0) {
				QMessageBox.critical(this, "Error", "Please add some items first");
				return;
			}
			
			delBtn.hide();
			productsWid.hide();
			newEntryWid.hide();
			formWid.show();
			
			currentPage = 2;
			nextPrevBtn.setText("<< Previous");
		}
		else if (currentPage == 2) {
			formWid.hide();
			
			delBtn.show();
			productsWid.show();
			newEntryWid.show();
			productCodeBox.setKeyboardFocus();
			
			currentPage = 1;
			nextPrevBtn.setText("Next >>");
		}
	}

	private void addProduct() {
		String productCode = productCodeBox.getText();

		if (productCode == null || productCode.equals("")) {
			QMessageBox.critical(this, "Error", "Please enter product code");
			return;
		}

		Product p = null;
		try {
			p = new Product(productCode);

			if (!p.isAvailable()) {
				QMessageBox.critical(this, "Error",
						"This product is currently not available");
				return;
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}

		if (p != null) {
			float qty = 0;

			if (p.getUnit() == Unit.NOS) {
				qty = (int) qtyBox.getVal();
			} else {
				qty = (float) qtyBox.getVal();
			}

			if (qty <= 0) {
				QMessageBox.critical(this, "Error",
						"Please enter valid quantity");
				return;
			}

			order.addProduct(p.getProductID(), qty);
			refreshProductsWid();
		}

		productNameBox.setText("");
		productCodeBox.setText("");
		qtyBox.setValue(1);

		productCodeBox.setKeyboardFocus();
	}

	private void refreshProductsWid() {
		productsWid.clearContents();

		productsWid.setRowCount(order.getProducts().size());

		int i = 0;
		for (String pID : order.getProducts().keySet()) {

			Product p = null;
			try {
				p = Product.getInstanceByID(pID);
			} catch (BilleException e) {
				order.removeProduct(pID);
			} catch (SQLException e) {
				// Do nothing
			}

			if (p != null) {
				QTableWidgetItem name = new QTableWidgetItem(p.getName());

				QTableWidgetItem qty = new QTableWidgetItem(
						Float.toString(order.getProducts().get(pID)));

				QTableWidgetItem unit = new QTableWidgetItem(p.getUnit()
						.toString());

				QTableWidgetItem ppu = new QTableWidgetItem(Float.toString(p
						.getPricePerUnit()));

				float totPrice = order.getProducts().get(pID)
						* p.getPricePerUnit();
				QTableWidgetItem totalPrice = new QTableWidgetItem(
						Float.toString(totPrice));

				productsWid.setItem(i, 0, name);
				productsWid.setItem(i, 1, qty);
				productsWid.setItem(i, 2, unit);
				productsWid.setItem(i, 3, ppu);
				productsWid.setItem(i, 4, totalPrice);

				i++;
			}
		}

		productsWid.repaint();
	}

	private void updateQty(SmartDoubleBox box) {
		int row = (Integer) box.property("rowNo");
		String productID = (String) order.getProducts().keySet().toArray()[row];

		order.getProducts().put(productID, (float) box.getVal());
		refreshProductsWid();
	}

	private void removeProducts() {
		for (QTableWidgetItem i : productsWid.selectedItems()) {
			int row = i.row();
			String productID = (String) order.getProducts().keySet().toArray()[row];

			order.removeProduct(productID);
		}
		
		refreshProductsWid();
		productCodeBox.setKeyboardFocus();
	}

	private void searchByName() {
		String sql = "SELECT * FROM products WHERE name LIKE " + "'%"
				+ productNameBox.getText() + "%' AND is_available=1";

		Product p = ProductsScreen.selectProduct(sql, this);
		if (p != null) {
			productNameBox.setText(p.getName());
			productCodeBox.setText(p.getCode());
			qtyBox.setFocus();
		}
	}

	private void assignCustomer() {
		String phNo = customerBox.getText();

		Customer c = null;
		if (phNo == null || phNo.equals("")) {
			c = NewCustomerScreen.createCustomer(this);
		} else {
			try {
				c = new Customer(customerBox.getText());
			} catch (BilleException e) {
				QMessageBox.critical(this, "Error", e.getMessage());
				return;
			} catch (SQLException e) {
				Application.showSystemError(this);
				return;
			}
		}

		if (c != null) {
			order.setCustomerID(c.getCustomerID());
			customerBox.setText(c.getPhoneNum());
			custNameLab.setText("<b style='color: #4096ee'>Customer: </b>"
					+ c.getFirstName() + " " + c.getLastName());
		}
	}

	private void placeOrder() {

		if (order.getProducts().size() <= 0) {
			QMessageBox.critical(this, "Error", "Please add some items first");
			return;
		}

		order.setComments(commentsBox.getText());
		order.setTakenBy(Application.getInstance().getLoggedInStaff()
				.getStaffID());
		order.setOrderID(UUID.randomUUID().toString());

		boolean r = RetailBillScreen.process(this, order);
		
		if (!r) {
			return;
		}

		/*
		 * try { int ret = order.createNew();
		 * 
		 * if (ret != 0) { QMessageBox.critical(this, "Error",
		 * order.getErrorMessage(ret)); return; } } catch (BilleException e) {
		 * QMessageBox.critical(this, "Error", e.getMessage()); return; } catch
		 * (SQLException e) { Application.showSystemError(this);
		 * System.err.println(e.getMessage()); return; }
		 */

		productsWid.clear();
		productNameBox.setText("");
		productCodeBox.setText("");
		qtyBox.setValue(1);
		customerBox.setText("");
		commentsBox.setText("");

		order = new RetailOrder();
	}

	private void showBillScreen() {

	}

	// Widget init functions

	private void initWidgets() {
		initInfoWid();
		initNewEntryWid();
		initFormWid();
		
		infoWid.setStyleSheet("border-bottom: solid 1px grey;");
		nextPrevBtn.setStyleSheet("background: none; color: #4096ee; font-size: bold;");
		nextPrevBtn.setCursor(new QCursor(CursorShape.PointingHandCursor));

		mainLay.addWidget(infoWid, 0, new Alignment(AlignmentFlag.AlignTop));
		mainLay.addWidget(productsWid);
		mainLay.addWidget(newEntryWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(nextPrevBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(formWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		
		formWid.hide();
		this.setLayout(mainLay);
	}

	private void initInfoWid() {
		Staff s = Application.getInstance().getLoggedInStaff();
		staffNameLab.setText("<b style='color: #4096ee'>Staff: </b>"
				+ s.getFirstName());
		custNameLab.setText("<b style='color: #4096ee'>Customer: </b>N/A");

		infoLay.addWidget(staffNameLab, 0, new Alignment(
				AlignmentFlag.AlignLeft));
		infoLay.addWidget(custNameLab, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		infoLay.addWidget(delBtn, 1, new Alignment(AlignmentFlag.AlignRight));

		infoWid.setLayout(infoLay);
	}

	private void initProductsWid() {
		productsWid.setEditTriggers(EditTrigger.NoEditTriggers);
		productsWid.setSelectionMode(SelectionMode.SingleSelection);
		productsWid.setAlternatingRowColors(true);

		productsWid.setColumnCount(5);

		productsWid.horizontalHeader().setResizeMode(
				QHeaderView.ResizeMode.Stretch);
		productsWid.verticalHeader().setResizeMode(
				QHeaderView.ResizeMode.ResizeToContents);

		productsWid.setHorizontalHeaderItem(0, new QTableWidgetItem(
				"Product Name"));
		productsWid
				.setHorizontalHeaderItem(1, new QTableWidgetItem("Quantity"));
		productsWid.setHorizontalHeaderItem(2, new QTableWidgetItem("Unit"));
		productsWid.setHorizontalHeaderItem(3, new QTableWidgetItem(
				"Price / Unit"));
		productsWid.setHorizontalHeaderItem(4, new QTableWidgetItem(
				"Price (Total)"));
		productsWid.setHorizontalHeaderItem(5, new QTableWidgetItem("Actions"));

	}
	
	private void initNewEntryWid() {
		qtyBox.setMin(0.1);
		qtyBox.setValue(1);

		newEntryLay.addWidget(productNameBox);
		newEntryLay.addWidget(productCodeBox);
		newEntryLay.addWidget(qtyBox);
		newEntryLay.addWidget(addBtn);
		
		newEntryWid.setLayout(newEntryLay);
	}

	private void initFormWid() {
		productNameBox.setPlaceholder("Product name");
		productCodeBox.setPlaceholder("Product code");
		qtyBox.setPlaceholder("Qty: ");
		customerBox.setPlaceholder("Customer phone no.");
		commentsBox.setPlaceholder("Comments");

		formLay.addWidget(customerBox, 0, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		formLay.addWidget(commentsBox, 0, 1, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));

		formLay.addWidget(proceedBtn, 1, 0, 1, 3, new Alignment(
				AlignmentFlag.AlignBottom, AlignmentFlag.AlignCenter));

		formWid.setLayout(formLay);
	}

	private RetailOrder order = new RetailOrder();
	private int currentPage = 1;

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QWidget infoWid = new QWidget(this);
	private QHBoxLayout infoLay = new QHBoxLayout(infoWid);
	private QLabel staffNameLab = new QLabel(infoWid);
	private QLabel custNameLab = new QLabel(infoWid);
	private RectButton delBtn = new RectButton(infoWid, "Delete",
			GradientColor.RED);
	
	private QTableWidget productsWid = new QTableWidget(this);

	private QWidget newEntryWid = new QWidget(this);
	private QHBoxLayout newEntryLay = new QHBoxLayout(newEntryWid);
	private SmartTextBox productNameBox = new SmartTextBox(newEntryWid);
	private SmartTextBox productCodeBox = new SmartTextBox(newEntryWid);
	private SmartDoubleBox qtyBox = new SmartDoubleBox(newEntryWid);
	private RectButton addBtn = new RectButton(newEntryWid, "Add",
			GradientColor.GREEN);
	
	private QPushButton nextPrevBtn = new QPushButton("Next >>", this);

	private QWidget formWid = new QWidget(this);
	private QGridLayout formLay = new QGridLayout(formWid);

	private SmartTextBox customerBox = new SmartTextBox(formWid);

	private SmartTextBox commentsBox = new SmartTextBox(formWid);

	private LongButton proceedBtn = new LongButton(formWid,
			"Proceed to Payment", GradientColor.BLUE);
}