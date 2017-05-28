package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Item;
import com.bille.PaymentType;
import com.bille.Product;
import com.bille.RestoOrder;
import com.bille.RetailOrder;
import com.bille.Staff;
import com.bille.Unit;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartDoubleBox;
import com.bille.widgets.SmartTextBox;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.core.Qt;

public class RestoBillScreen extends QDialog {

	private RestoBillScreen(QWidget parent, RestoOrder o)
			throws BilleException, SQLException {
		super (parent);
		this.order = o;
		this.setWindowTitle("Finalise Order");

		initWidgets();
		doSignals();
	}
	
	public static boolean process(QWidget parent, RestoOrder o) {
		RestoBillScreen s = null;
		
		try {
			s = new RestoBillScreen(parent, o);
		} catch (Exception e) {
			return false;
		}
		
		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();
		if (ok) {
			return true;
		}
		
		return false;
	}

	private void doSignals() {
		paymentTypeBox.currentIndexChanged.connect(this,
				"paymentTypeChangedHandler(int)");
		
		amtPaidBox.valueChanged.connect(this, "updateCashDetails(double)");
		cancelBtn.clicked.connect(this, "reject()");
	}

	// Slots

	private void paymentTypeChangedHandler(int index) {
		if (index == 0 | index == 1) {
			discountReasonBox.setText("");

			amtPaidLab.setEnabled(false);
			amtPaidBox.setEnabled(false);
			amtPaidBox.setValue(0);

			cashToBeReturnedLab.setEnabled(false);
			cashToBeReturnedValLab.setEnabled(false);
			cashToBeReturnedValLab.setText("0");

			discountLab.setEnabled(false);
			discountValLab.setEnabled(false);
			discountValLab.setText("0");

			discountReasonBox.setEnabled(false);
			discountReasonBox.setText("");
			
			amtToBePaid = order.getBillAmount();
			billAmtValLab.setText(Float.toString(amtToBePaid));
		} else {
			amtPaidLab.setEnabled(true);
			amtPaidBox.setEnabled(true);

			cashToBeReturnedLab.setEnabled(true);
			cashToBeReturnedValLab.setEnabled(true);

			discountLab.setEnabled(true);
			discountValLab.setEnabled(true);
			discountReasonBox.setEnabled(true);
			
			amtToBePaid = Math.round(order.getBillAmount());
			billAmtValLab.setText(Float.toString(amtToBePaid));
		}
	}
	
	private void updateCashDetails(double amtPaid) {
		if (amtPaid < amtToBePaid) {
			float discount = amtToBePaid - (float) amtPaid;
			discountApplied = true;
			discountValLab.setText(Float.toString(discount));
			
			return;
		}
		
		discountApplied = false;
		discountValLab.setText("0");
		float cashToBeReturned = (float) amtPaid - amtToBePaid;
		cashToBeReturnedValLab.setText(Float.toString(cashToBeReturned));
	}

	// Widget functions

	private void initInfoLab() throws BilleException, SQLException {
		String customerName = "N/A";
		String customerPhNo = "N/A";

		try {
			Customer c = Customer.getInstanceByID(order.getCustomerID());
			customerName = c.getFirstName() + " " + c.getLastName();
			customerPhNo = c.getPhoneNum();
		} catch (Exception e) {

		}

		Staff s = Staff.getInstanceByID(order.getTakenBy());
		String staffName = s.getFirstName();

		String labTxt = "";
		labTxt += "<b style='color: #4096ee;'>";
		labTxt += "Customer Name: ";
		labTxt += "</b>";
		labTxt += customerName;
		labTxt += " ";
		labTxt += "<b style='color: #4096ee;'>";
		labTxt += "Ph. No: ";
		labTxt += "</b>";
		labTxt += customerPhNo;
		labTxt += " ";
		labTxt += "<b style='color: #4096ee;'>";
		labTxt += "Staff Name: ";
		labTxt += "</b>";
		labTxt += staffName;

		infoLab.setText(labTxt);
	}

	private void initBillTable() throws BilleException, SQLException {
		float billAmt = 0;
		
		billTable.setRowCount(order.getItems().size());
		billTable.setEditTriggers(EditTrigger.NoEditTriggers);
		billTable.setSelectionMode(SelectionMode.NoSelection);
		billTable.setAlternatingRowColors(true);

		billTable.setColumnCount(4);

		billTable.horizontalHeader().setResizeMode(
				QHeaderView.ResizeMode.Stretch);

		billTable.setHorizontalHeaderItem(0, new QTableWidgetItem(
				"Item Name"));
		billTable
				.setHorizontalHeaderItem(1, new QTableWidgetItem("Quantity"));
		billTable.setHorizontalHeaderItem(2, new QTableWidgetItem(
				"Price"));
		billTable.setHorizontalHeaderItem(3, new QTableWidgetItem(
				"Price (Total)"));
		
		int i = 0;
		for (String itmID : order.getItems().keySet()) {
			Item itm = Item.getInstanceByID(itmID);
			float total = itm.getPrice() * order.getItems().get(itmID);

			QTableWidgetItem name = new QTableWidgetItem(itm.getName());
			QTableWidgetItem qty = new QTableWidgetItem(Float.toString(order.getItems().get(itmID)));
		
			QTableWidgetItem price = new QTableWidgetItem(Float.toString(itm.getPrice()));

			float totPrice = order.getItems().get(itmID)
					* itm.getPrice();
			QTableWidgetItem totalPrice = new QTableWidgetItem(
					Float.toString(totPrice));
			
			billTable.setItem(i, 0, name);
			billTable.setItem(i, 1, qty);
			billTable.setItem(i, 2, price);
			billTable.setItem(i, 3, totalPrice);

			billAmt += total;
			i++;	
		}

		order.setBillAmount(billAmt);
		billAmtValLab.setText(Float.toString(billAmt));
	}

	private void initPaymentWid() {
		amtPaidBox.setMax(100000);
		paymentTypeBox.addItem("-- Select --");

		for (PaymentType p : PaymentType.values()) {
			paymentTypeBox.addItem(p.toString());
		}

		discountReasonBox.setPlaceholder("Discount reason");
		amtPaidLab.setEnabled(false);
		amtPaidBox.setEnabled(false);
		cashToBeReturnedLab.setEnabled(false);
		cashToBeReturnedValLab.setEnabled(false);
		discountLab.setEnabled(false);
		discountValLab.setEnabled(false);
		discountReasonBox.setEnabled(false);

		paymentLay.addWidget(billAmtLab, 0, 0);
		paymentLay.addWidget(billAmtValLab, 0, 1);
		paymentLay.addWidget(paymentTypeLab, 0, 2);
		paymentLay.addWidget(paymentTypeBox, 0, 3);

		paymentLay.addWidget(amtPaidLab, 1, 0);
		paymentLay.addWidget(amtPaidBox, 1, 1);
		paymentLay.addWidget(cashToBeReturnedLab, 1, 2);
		paymentLay.addWidget(cashToBeReturnedValLab, 1, 3);

		paymentLay.addWidget(discountLab, 2, 0, 1, 1, new Qt.Alignment(AlignmentFlag.AlignLeft));
		paymentLay.addWidget(discountValLab, 2, 1, 1, 2, new Qt.Alignment(AlignmentFlag.AlignLeft));
		paymentLay.addWidget(discountReasonBox, 2, 2, 1, 3, new Qt.Alignment(AlignmentFlag.AlignLeft));

		paymentWid.setLayout(paymentLay);
	}

	private void initButtonsWid() {
		buttonsLay.addWidget(cancelBtn);
		buttonsLay.addWidget(okBtn);

		buttonsWid.setLayout(buttonsLay);
	}

	private void initWidgets() throws BilleException, SQLException {
		initInfoLab();
		initBillTable();
		initPaymentWid();
		initButtonsWid();

		mainLay.addWidget(infoLab);
		mainLay.addWidget(billTable);
		mainLay.addWidget(paymentWid);
		mainLay.addWidget(buttonsWid);

		this.setLayout(mainLay);
	}

	private RestoOrder order;
	private float amtToBePaid;
	private boolean discountApplied;

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QLabel infoLab = new QLabel(this);
	private QTableWidget billTable = new QTableWidget(this);

	private QWidget paymentWid = new QWidget(this);
	private QGridLayout paymentLay = new QGridLayout(paymentWid);

	private QLabel billAmtLab = new QLabel("<b>Bill Amount: </b>", paymentWid);
	private QLabel billAmtValLab = new QLabel(paymentWid);

	private QLabel paymentTypeLab = new QLabel("<b>Payment type: </b>", paymentWid);
	private QComboBox paymentTypeBox = new QComboBox(paymentWid);

	private QLabel amtPaidLab = new QLabel("<b>Amount Paid: </b>", paymentWid);
	private SmartDoubleBox amtPaidBox = new SmartDoubleBox(paymentWid);

	private QLabel cashToBeReturnedLab = new QLabel("<b>Cash to be returned: </b>",
			paymentWid);
	private QLabel cashToBeReturnedValLab = new QLabel("0", paymentWid);

	private QLabel discountLab = new QLabel("<b>Discount: </b>", paymentWid);
	private QLabel discountValLab = new QLabel("0", paymentWid);
	private SmartTextBox discountReasonBox = new SmartTextBox(paymentWid);

	private QWidget buttonsWid = new QWidget(this);
	private QHBoxLayout buttonsLay = new QHBoxLayout(buttonsWid);
	private LongButton cancelBtn = new LongButton(buttonsWid, "Cancel",
			GradientColor.RED);
	private LongButton okBtn = new LongButton(buttonsWid, "OK",
			GradientColor.GREEN);
}