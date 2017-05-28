package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.Date;
import java.util.UUID;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Item;
import com.bille.RestoOrder;
import com.bille.RestoOrderType;
import com.bille.Staff;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartNumberBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;

public class EditActiveOrderScreen extends QFrame {

	public EditActiveOrderScreen(QWidget parent, RestoOrder o) {
		super(parent);
		this.order = o;

		initItemsWid();
		initWidgets();

		readFields();
		doSignals();
	}

	private void doSignals() {
		itemNameBox.returnPressed.connect(this, "searchByName()");
		addBtn.clicked.connect(this, "addItem()");
		customerBox.returnPressed.connect(this, "assignCustomer()");
		delBtn.clicked.connect(this, "removeItems()");

		closeBtn.clicked.connect(this, "closeOrder()");
		discardBtn.clicked.connect(this, "discardOrder()");
		saveBtn.clicked.connect(this, "saveOrder()");
		
		itemCodeBox.returnPressed.connect(this, "addItem()");
		nextPrevBtn.clicked.connect(this, "nextPrev()");
	}

	// Control functions
	
	private void nextPrev() {
		if (currentPage == 1) {
			if (order.getItems().size() <= 0) {
				QMessageBox.critical(this, "Error", "Please add some items first");
				return;
			}
			
			delBtn.hide();
			itemsWid.hide();
			newEntryWid.hide();
			formWid.show();
			
			currentPage = 2;
			nextPrevBtn.setText("<< Previous");
		}
		else if (currentPage == 2) {
			formWid.hide();
			
			delBtn.show();
			itemsWid.show();
			newEntryWid.show();
			itemCodeBox.setKeyboardFocus();
			
			currentPage = 1;
			nextPrevBtn.setText("Next >>");
		}
	}

	private void readFields() {
		try {
			Customer c = Customer.getInstanceByID(order.getCustomerID());

			customerBox.setText(c.getPhoneNum());
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}

		tableNoBox.setValue(order.getTableNo());
		commentsBox.setText(order.getComments());

		RestoOrderType type = order.getOrderType();

		if (type == RestoOrderType.DINE_IN) {
			dineInRad.setChecked(true);
		} else if (type == RestoOrderType.TAKEAWAY) {
			takeawayRad.setChecked(true);
		} else if (type == RestoOrderType.DELIVERY) {
			deliveryRad.setChecked(true);
		}

		itemsWid.setRowCount(order.getItems().size());
		int i = 0;
		for (String itmID : order.getItems().keySet()) {

			Item itm = null;
			try {
				itm = Item.getInstanceByID(itmID);
			} catch (BilleException e) {
				order.removeItem(itmID);
			} catch (SQLException e) {
				// Do nothing
			}

			if (itm != null) {
				QTableWidgetItem name = new QTableWidgetItem(itm.getName());

				QTableWidgetItem qty = new QTableWidgetItem(
						Integer.toString(order.getItems().get(itmID)));

				QTableWidgetItem price = new QTableWidgetItem(
						Float.toString(itm.getPrice()));

				float totPrice = order.getItems().get(itmID) * itm.getPrice();
				QTableWidgetItem totalPrice = new QTableWidgetItem(
						Float.toString(totPrice));
			
				itemsWid.setItem(i, 0, name);
				itemsWid.setItem(i, 1, qty);
				itemsWid.setItem(i, 2, price);
				itemsWid.setItem(i, 3, totalPrice);

				itemsWid.resizeColumnsToContents();
				i++;
			}
		}
	}

	private void addItem() {
		String itemCode = itemCodeBox.getText();

		if (itemCode == null || itemCode.equals("")) {
			QMessageBox.critical(this, "Error", "Please enter item code");
			return;
		}

		Item i = null;
		try {
			i = new Item(itemCode);

			if (!i.isAvailable()) {
				QMessageBox.critical(this, "Error",
						"This item is currently not available");
				return;
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			System.err.println(e.getMessage());
			Application.showSystemError(this);
			return;
		}

		if (i != null) {
			int qty = qtyBox.getVal();

			if (qty <= 0) {
				QMessageBox.critical(this, "Error",
						"Please enter valid quantity");
				return;
			}

			order.addItem(i.getItemID(), qty);
			refreshItemsWid();
		}

		itemNameBox.setText("");
		itemCodeBox.setText("");
		qtyBox.setValue(0);
		
		itemCodeBox.setKeyboardFocus();
	}

	private void refreshItemsWid() {
		itemsWid.clearContents();
		itemsWid.setRowCount(order.getItems().size());

		int i = 0;
		for (String itmID : order.getItems().keySet()) {

			Item itm = null;
			try {
				itm = Item.getInstanceByID(itmID);
			} catch (BilleException e) {
				order.removeItem(itmID);
			} catch (SQLException e) {
				// Do nothing
			}

			if (itm != null) {
				QTableWidgetItem name = new QTableWidgetItem(itm.getName());

				QTableWidgetItem qty = new QTableWidgetItem(
						Integer.toString(order.getItems().get(itmID)));
				
				QTableWidgetItem price = new QTableWidgetItem(
						Float.toString(itm.getPrice()));

				float totPrice = order.getItems().get(itmID) * itm.getPrice();
				QTableWidgetItem totalPrice = new QTableWidgetItem(
						Float.toString(totPrice));

				itemsWid.setItem(i, 0, name);
				itemsWid.setItem(i, 1, qty);
				itemsWid.setItem(i, 2, price);
				itemsWid.setItem(i, 3, totalPrice);

				i++;
			}
		}

		itemsWid.repaint();
	}

	private void updateQty(SmartNumberBox box) {
		int row = (Integer) box.property("rowNo");
		String productID = (String) order.getItems().keySet().toArray()[row];

		order.getItems().put(productID, box.getVal());
		refreshItemsWid();
	}

	private void removeItems() {
		for (QTableWidgetItem i : itemsWid.selectedItems()) {
			int row = i.row();
			String itemID = (String) order.getItems().keySet().toArray()[row];

			order.removeItem(itemID);
		}

		refreshItemsWid();
		itemCodeBox.setKeyboardFocus();
	}

	private void searchByName() {
		String sql = "SELECT * FROM items WHERE name LIKE '%"
				+ itemNameBox.getText() + "%' AND is_available=1";

		Item i = ItemsScreen.selectItem(sql, this);
		if (i != null) {
			itemNameBox.setText(i.getName());
			itemCodeBox.setText(i.getCode());
			qtyBox.setFocus();
		}
	}

	private void assignCustomer() {
		String phNo = customerBox.getText();

		Customer c = null;
		if (phNo == null || phNo.equals("")) {
			c = NewCustomerScreen.createCustomer(this);
			if (c != null) {
				order.setCustomerID(c.getCustomerID());
				customerBox.setText(c.getPhoneNum());
			}
		} else {
			try {
				c = new Customer(customerBox.getText());
			} catch (BilleException e) {
				QMessageBox.critical(this, "Error", e.getMessage());
			} catch (SQLException e) {
				Application.showSystemError(this);
			}
		}

		if (c != null) {
			order.setCustomerID(c.getCustomerID());
			customerBox.setText(c.getPhoneNum());
			custNameLab.setText("<b style='color: #4096ee'>Customer: </b>"
					+ c.getFirstName() + " " + c.getLastName());
		}
	}

	private void saveOrder() {
		if (order.getItems().size() <= 0) {
			QMessageBox.critical(this, "Error", "Please add some items first");
			return;
		}

		RestoOrderType orderType = null;
		int tableNo = 0;

		if (dineInRad.isChecked()) {
			orderType = RestoOrderType.DINE_IN;
		} else if (takeawayRad.isChecked()) {
			orderType = RestoOrderType.TAKEAWAY;
		} else if (deliveryRad.isChecked()) {
			orderType = RestoOrderType.DELIVERY;
		}

		if (orderType == null) {
			QMessageBox.critical(this, "Error", "Please select an order type");
			return;
		} else if (orderType == RestoOrderType.DINE_IN) {
			tableNo = tableNoBox.getVal();
			if (tableNo <= 0) {
				QMessageBox.critical(this, "Error",
						"Please select a valid table number");
				return;
			}
		}

		order.setOrderType(orderType);
		order.setTableNo(tableNo);
		order.setComments(commentsBox.getText());

		try {
			int ret = order.update();

			if (ret != 0) {
				QMessageBox.critical(this, "Error", order.getErrorMessage(ret));
				return;
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			System.err.println(e.getMessage());
			return;
		}

		QMessageBox.information(this, "Success", "Order updated");
	}

	private void closeOrder() {

	}

	private void discardOrder() {
		try {

			QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();
			buttons.set(QMessageBox.StandardButton.Yes);
			buttons.set(QMessageBox.StandardButton.No);
			QMessageBox.StandardButton ret = QMessageBox.warning(this,
					tr("Delete Order"),
					tr("Are you sure you want to delete this order?"), buttons,
					QMessageBox.StandardButton.No);

			if (ret.equals(QMessageBox.StandardButton.Yes)) {
				RestoOrder.delete(order.getOrderID());
				this.disposeLater();

				new Application().showHomeScreen();
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			Application.showSystemError(this);
			return;
		}
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
		mainLay.addWidget(itemsWid);
		mainLay.addWidget(newEntryWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(nextPrevBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(formWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		
		formWid.hide();
		this.setLayout(mainLay);
	}

	private void initItemsWid() {
		itemsWid.setEditTriggers(EditTrigger.NoEditTriggers);
		itemsWid.setSelectionMode(SelectionMode.SingleSelection);

		itemsWid.setColumnCount(4);

		itemsWid.horizontalHeader().setResizeMode(
				QHeaderView.ResizeMode.Stretch);
		itemsWid.verticalHeader().setResizeMode(
				QHeaderView.ResizeMode.ResizeToContents);

		itemsWid.setHorizontalHeaderItem(0, new QTableWidgetItem("Item name"));
		itemsWid.setHorizontalHeaderItem(1, new QTableWidgetItem("Quantity"));
		itemsWid.setHorizontalHeaderItem(2, new QTableWidgetItem("Price"));
		itemsWid.setHorizontalHeaderItem(3, new QTableWidgetItem("Total price"));
		itemsWid.setHorizontalHeaderItem(4, new QTableWidgetItem("Actions"));
	}

	private void initInfoWid() {
		Staff s = Application.getInstance().getLoggedInStaff();
		staffNameLab.setText("<b style='color: #4096ee'>Staff: </b>"
				+ s.getFirstName());

		try {
			Customer c = Customer.getInstanceByID(order.getCustomerID());
			custNameLab.setText("<b style='color: #4096ee'>Customer: </b>"
					+ c.getFirstName() + " " + c.getLastName());
		} catch (Exception e) {
			custNameLab.setText("<b style='color: #4096ee'>Customer: </b>N/A");
		}

		infoLay.addWidget(staffNameLab, 0, new Alignment(
				AlignmentFlag.AlignLeft));
		infoLay.addWidget(custNameLab, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		infoLay.addWidget(delBtn, 1, new Alignment(AlignmentFlag.AlignRight));

		infoWid.setLayout(infoLay);
	}
	
	private void initNewEntryWid() {
		qtyBox.setMin(1);

		itemNameBox.setPlaceholder("Item name");
		itemCodeBox.setPlaceholder("Item code");
		qtyBox.setPlaceholder("Qty: ");
		
		newEntryLay.addWidget(itemNameBox);
		newEntryLay.addWidget(itemCodeBox);
		newEntryLay.addWidget(qtyBox);
		newEntryLay.addWidget(addBtn);
		
		newEntryWid.setLayout(newEntryLay);
	}

	private void initFormWid() {
		initOrderTypeWid();
		
		customerBox.setPlaceholder("Customer phone no.");
		tableNoBox.setPlaceholder("Table no: ");
		commentsBox.setPlaceholder("Comments");

		formLay.addWidget(customerBox, 0, 0);
		formLay.addWidget(tableNoBox, 0, 1);
		formLay.addWidget(commentsBox, 0, 2);

		formLay.addWidget(orderTypeWid, 1, 0, 1, 3, new Alignment(
				AlignmentFlag.AlignCenter));

		formLay.addWidget(closeBtn, 2, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		formLay.addWidget(discardBtn, 2, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		formLay.addWidget(saveBtn, 2, 2, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		formWid.setLayout(formLay);
	}

	private void initOrderTypeWid() {
		orderTypeLay.addWidget(orderTypeLab);
		orderTypeLay.addWidget(dineInRad);
		orderTypeLay.addWidget(takeawayRad);
		orderTypeLay.addWidget(deliveryRad);

		orderTypeWid.setLayout(orderTypeLay);
	}

	private RestoOrder order = new RestoOrder();
	private int currentPage = 1;

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QWidget infoWid = new QWidget(this);
	private QHBoxLayout infoLay = new QHBoxLayout(infoWid);
	private QLabel staffNameLab = new QLabel(infoWid);
	private QLabel custNameLab = new QLabel(infoWid);
	private RectButton delBtn = new RectButton(infoWid, "Delete",
			GradientColor.RED);

	private QTableWidget itemsWid = new QTableWidget(this);
	
	private QWidget newEntryWid = new QWidget(this);
	private QHBoxLayout newEntryLay = new QHBoxLayout(newEntryWid);
	private SmartTextBox itemNameBox = new SmartTextBox(newEntryWid);
	private SmartTextBox itemCodeBox = new SmartTextBox(newEntryWid);
	private SmartNumberBox qtyBox = new SmartNumberBox(newEntryWid);
	private RectButton addBtn = new RectButton(newEntryWid, "Add",
			GradientColor.GREEN);
	
	private QPushButton nextPrevBtn = new QPushButton("Next >>", this);

	private QWidget formWid = new QWidget(this);
	private QGridLayout formLay = new QGridLayout(formWid);

	private SmartTextBox customerBox = new SmartTextBox(formWid);
	private SmartNumberBox tableNoBox = new SmartNumberBox(formWid);

	private QWidget orderTypeWid = new QWidget(formWid);
	private QLabel orderTypeLab = new QLabel("Order type: ", orderTypeWid);
	private QHBoxLayout orderTypeLay = new QHBoxLayout(orderTypeWid);
	private QRadioButton dineInRad = new QRadioButton("Dine In", orderTypeWid);
	private QRadioButton takeawayRad = new QRadioButton("Takeaway",
			orderTypeWid);
	private QRadioButton deliveryRad = new QRadioButton("Delivery",
			orderTypeWid);

	private SmartTextBox commentsBox = new SmartTextBox(formWid);

	private LongButton closeBtn = new LongButton(formWid, "Close",
			GradientColor.BLUE);
	private LongButton discardBtn = new LongButton(formWid, "Discard",
			GradientColor.RED);
	private LongButton saveBtn = new LongButton(formWid, "Save",
			GradientColor.GREEN);
}