package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.Application;
import com.bille.RestoOrder;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;
import com.trolltech.qt.gui.QSizePolicy.Policy;

public class ActiveOrdersScreen extends QFrame {

	public ActiveOrdersScreen(QWidget parent) {
		super(parent);

		initWidgets();
		ordersList.setAlternatingRowColors(true);

		readActiveOrders();
		doSignals();
	}

	private void doSignals() {
		showBtn.clicked.connect(this, "showOrder()");
	}

	// Control functions

	private void readActiveOrders() {
		try {
			String[] activeOrders = RestoOrder.getActiveOrders();
			
			if (activeOrders == null) {
				return;
			}

			for (String orderID : activeOrders) {
				if (RestoOrder.checkOrderExists(orderID)) {
					RestoOrder o = new RestoOrder(orderID);
					this.activeOrders.add(orderID);
					ordersList.addItem(o.getOrderID());
				}
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}
	}

	// Slots

	private void showOrder() {
		int currentIndex = ordersList.currentRow();

		if (currentIndex < 0) {
			QMessageBox.critical(this, "Error", "Please select an order");
			return;
		}

		String orderID = activeOrders.get(currentIndex);

		RestoOrder o = null;

		try {
			o = new RestoOrder(orderID);
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}

		Application a = new Application();
		a.showEditActiveOrderScreen(o);
	}

	// Widget functions

	private void initWidgets() {
		ordersList.setProperty("big_items", true);

		mainLay.addWidget(ordersList);
		mainLay.addWidget(showBtn, 0, new Qt.Alignment(
				AlignmentFlag.AlignCenter));
		this.setLayout(mainLay);
	}

	private ArrayList<String> activeOrders = new ArrayList<String>();

	private QVBoxLayout mainLay = new QVBoxLayout(this);
	private QListWidget ordersList = new QListWidget(this);
	private LongButton showBtn = new LongButton(this, "Show",
			GradientColor.GREEN);

}
