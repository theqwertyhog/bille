package com.bille.ui.screens;

import com.bille.Application;
import com.bille.StoreType;
import com.bille.UserRole;
import com.bille.ui.GradientColor;
import com.bille.widgets.LargeButton;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QLayout.SizeConstraint;

public class HomeScreen extends QFrame {

	public HomeScreen(QWidget parent) {
		super(parent);

		initWidgets();
		doSignals();
	}

	private void doSignals() {
		Application a = new Application();

		newOrderBtn.clicked.connect(a, "showNewOrderScreen()");
		activeOrdersBtn.clicked.connect(a, "showActiveOrdersScreen()");

		catBtn.clicked.connect(a, "showCatScreen()");
		itemsBtn.clicked.connect(a, "showItemsScreen()");
		productsBtn.clicked.connect(a, "showProductsScreen()");
		taxesBtn.clicked.connect(a, "showTaxesScreen()");

		custBtn.clicked.connect(a, "showCustomersScreen()");
		staffBtn.clicked.connect(a, "showStaffScreen()");
		settingsBtn.clicked.connect(a, "showSettingsScreen()");
	}

	public void initWidgets() {
		initOrdersWid();
		initInventoryWid();
		initPeopleWid();
		initStoreWid();

		mainLay.addWidget(ordersWid, 0, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(inventoryWid, 0, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(peopleWid, 1, 0, 1, 1, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(storeWid, 1, 1, 1, 2, new Alignment(AlignmentFlag.AlignCenter));
		
		this.setLayout(mainLay);
	}

	// Initialise individual widgets

	private void initOrdersWid() {
		ordersLay.addWidget(ordersLab, 0, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
		
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			ordersLay.addWidget(newOrderBtn, 1, 0, 1, 1, new Alignment(
					AlignmentFlag.AlignCenter));
			ordersLay.addWidget(activeOrdersBtn, 1, 1, 1, 1, new Alignment(
					AlignmentFlag.AlignCenter));
		} else {
			activeOrdersBtn.hide();
			ordersLay.addWidget(newOrderBtn, 1, 0, 1, 2, new Alignment(
					AlignmentFlag.AlignCenter));
		}

		ordersWid.setLayout(ordersLay);
	}

	private void initInventoryWid() {
		inventoryLay.addWidget(inventoryLab, 0, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
		inventoryLay.addWidget(catBtn, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			productsBtn.hide();
			inventoryLay.addWidget(itemsBtn, 1, 1, 1, 1, new Alignment(
					AlignmentFlag.AlignCenter));
		} else if (Application.getInstance().getStore().getType() == StoreType.RETAIL) {
			itemsBtn.hide();
			inventoryLay.addWidget(productsBtn, 1, 1, 1, 1, new Alignment(
					AlignmentFlag.AlignCenter));
		}

		inventoryWid.setLayout(inventoryLay);
	}

	private void initPeopleWid() {

		if (Application.getInstance().getLoggedInStaff().getRole() != UserRole.ADMIN) {
			custBtn.setEnabled(false);
			staffBtn.setEnabled(false);
		}

		peopleLay.addWidget(peopleLab, 0, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
		peopleLay.addWidget(custBtn, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		peopleLay.addWidget(staffBtn, 1, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		peopleWid.setLayout(peopleLay);
	}

	private void initStoreWid() {
		if (Application.getInstance().getLoggedInStaff().getRole() != UserRole.ADMIN) {
			settingsBtn.setEnabled(false);
		}
		
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			storeLab.setText("Restaurant");
		}

		storeLay.addWidget(storeLab, 0, 0, 1, 2, new Alignment(
				AlignmentFlag.AlignCenter));
		storeLay.addWidget(taxesBtn, 1, 0, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));
		storeLay.addWidget(settingsBtn, 1, 1, 1, 1, new Alignment(
				AlignmentFlag.AlignCenter));

		storeWid.setLayout(storeLay);
	}

	private QGridLayout mainLay = new QGridLayout(this);

	private QWidget ordersWid = new QWidget(this);
	private QGridLayout ordersLay = new QGridLayout(ordersWid);
	private QLabel ordersLab = new QLabel("Orders", ordersWid);
	private LargeButton newOrderBtn = new LargeButton(ordersWid, "New",
			GradientColor.GREY);
	private LargeButton activeOrdersBtn = new LargeButton(ordersWid, "Active",
			GradientColor.GREY);

	private QWidget inventoryWid = new QWidget(this);
	private QGridLayout inventoryLay = new QGridLayout(inventoryWid);
	private QLabel inventoryLab = new QLabel("Inventory", inventoryWid);
	private LargeButton catBtn = new LargeButton(inventoryWid, "Categories",
			GradientColor.BLUE);
	private LargeButton itemsBtn = new LargeButton(inventoryWid, "Items",
			GradientColor.BLUE);
	private LargeButton productsBtn = new LargeButton(inventoryWid, "Products",
			GradientColor.BLUE);

	private QWidget peopleWid = new QWidget(this);
	private QGridLayout peopleLay = new QGridLayout(peopleWid);
	private QLabel peopleLab = new QLabel("People", peopleWid);
	private LargeButton custBtn = new LargeButton(peopleWid, "Customers",
			GradientColor.BLUE);
	private LargeButton staffBtn = new LargeButton(peopleWid, "Staff",
			GradientColor.BLUE);

	private QWidget storeWid = new QWidget(this);
	private QGridLayout storeLay = new QGridLayout(storeWid);
	private QLabel storeLab = new QLabel("Store", storeWid);
	private LargeButton taxesBtn = new LargeButton(storeWid, "Taxes",
			GradientColor.GREY);
	private LargeButton settingsBtn = new LargeButton(storeWid, "Settings",
			GradientColor.GREY);

}
