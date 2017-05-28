package com.bille.widgets;

import com.bille.Application;
import com.bille.StoreType;
import com.bille.UserRole;
import com.bille.ui.GradientColor;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class MenuBar extends QFrame {

	public MenuBar(QWidget parent) {
		super(parent);
		this.setObjectName("MenuBar");

		initWidgets();
		doSignals();
	}
	
	private void doSignals() {
		Application a = new Application();
		
		homeBtn.clicked.connect(a, "showHomeScreen()");
		newOrderBtn.clicked.connect(a, "showNewOrderScreen()");
		activeOrdersBtn.clicked.connect(a, "showActiveOrdersScreen()");
		
		catBtn.clicked.connect(a, "showCatScreen()");
		itemBtn.clicked.connect(a, "showItemsScreen()");
		productBtn.clicked.connect(a, "showProductsScreen()");
		taxesBtn.clicked.connect(a, "showTaxesScreen()");
		
		custBtn.clicked.connect(a, "showCustomersScreen()");
		staffBtn.clicked.connect(a, "showStaffScreen()");
		settingsBtn.clicked.connect(a, "showSettingsScreen()");
		
		logoutBtn.clicked.connect(a, "logout()");
	}


	private void initWidgets() {
		initButtons();
		
		if (storeType == StoreType.RESTO) {
			productBtn.hide();
		} else if (storeType == StoreType.RETAIL) {
			activeOrdersBtn.hide();
			itemBtn.hide();
		}
		
		if (userRole != UserRole.ADMIN) {
			custBtn.setEnabled(false);
			staffBtn.setEnabled(false);
			settingsBtn.setEnabled(false);
		}

		leftLay.addWidget(homeBtn);
		leftLay.addWidget(newOrderBtn);
		leftLay.addWidget(activeOrdersBtn);
		leftWid.setLayout(leftLay);

		midLay.addWidget(catBtn);
		midLay.addWidget(taxesBtn);
		midLay.addWidget(itemBtn);
		midLay.addWidget(productBtn);
		midWid.setLayout(midLay);

		rightLay.addWidget(custBtn);
		rightLay.addWidget(staffBtn);
		rightLay.addWidget(settingsBtn);
		rightLay.addWidget(logoutBtn);
		rightWid.setLayout(rightLay);

		mainLay.addWidget(leftWid, 0, new Alignment(AlignmentFlag.AlignTop));
		mainLay.addWidget(midWid, 0, new Alignment(AlignmentFlag.AlignVCenter));
		mainLay.addWidget(rightWid, 0, new Alignment(AlignmentFlag.AlignBottom));

		this.setLayout(mainLay);
	}

	private void initButtons() {
		homeBtn
		.setStyleSheet("background-image: url('resources/icons/42x42/add.png'); background-repeat: no-repeat; background-position: center;");
		newOrderBtn
				.setStyleSheet("background-image: url('resources/icons/42x42/add.png'); background-repeat: no-repeat; background-position: center;");
		activeOrdersBtn
				.setStyleSheet("background-image: url('resources/icons/42x42/active.png'); background-repeat: no-repeat; background-position: center;");

		catBtn.setStyleSheet("background-image: url('resources/icons/42x42/cat.png'); background-repeat: no-repeat; background-position: center;");
		itemBtn.setStyleSheet("background-image: url('resources/icons/42x42/item.png'); background-repeat: no-repeat; background-position: center;");
		productBtn
				.setStyleSheet("background-image: url('resources/icons/42x42/product.png'); background-repeat: no-repeat; background-position: center;");
		taxesBtn.setStyleSheet("background-image: url('resources/icons/42x42/currency.png'); background-repeat: no-repeat; background-position: center;");

		custBtn.setStyleSheet("background-image: url('resources/icons/42x42/cust.png'); background-repeat: no-repeat; background-position: center;");
		staffBtn.setStyleSheet("background-image: url('resources/icons/42x42/staff.png'); background-repeat: no-repeat; background-position: center;");
		settingsBtn
				.setStyleSheet("background-image: url('resources/icons/42x42/settings.png'); background-repeat: no-repeat; background-position: center;");
		logoutBtn
				.setStyleSheet("background-image: url('resources/icons/42x42/logoff.png'); background-repeat: no-repeat; background-position: center;");
	
		newOrderBtn.setToolTip("New Order");
	}

	private StoreType storeType = Application.getInstance().getStore().getType();
	private UserRole userRole = Application.getInstance().getLoggedInStaff().getRole();

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QWidget leftWid = new QWidget(this);
	private QVBoxLayout leftLay = new QVBoxLayout(leftWid);
	private MediumButton homeBtn = new MediumButton(leftWid, "", GradientColor.TRANSPARENT);
	private MediumButton newOrderBtn = new MediumButton(leftWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton activeOrdersBtn = new MediumButton(leftWid, "",
			GradientColor.TRANSPARENT);

	private QWidget midWid = new QWidget(this);
	private QVBoxLayout midLay = new QVBoxLayout(midWid);
	private MediumButton catBtn = new MediumButton(midWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton itemBtn = new MediumButton(midWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton productBtn = new MediumButton(midWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton taxesBtn = new MediumButton(midWid, "",
			GradientColor.TRANSPARENT);

	private QWidget rightWid = new QWidget(this);
	private QVBoxLayout rightLay = new QVBoxLayout(rightWid);
	private MediumButton custBtn = new MediumButton(rightWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton staffBtn = new MediumButton(rightWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton settingsBtn = new MediumButton(rightWid, "",
			GradientColor.TRANSPARENT);
	private MediumButton logoutBtn = new MediumButton(rightWid, "",
			GradientColor.TRANSPARENT);

}