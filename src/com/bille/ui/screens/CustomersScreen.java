package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Staff;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.RectButton;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.ApplicationAttribute;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

public class CustomersScreen extends QMainWindow {
	
	public CustomersScreen(String sql, QWidget parent) {
		super(parent);
		this.originalSQL = sql;
		this.sql = sql;
		this.originalSQL = sql;

		this.setWindowTitle("Customers");

		buildSQL();

		try {
			fetchRows();
		} catch (SQLException e) {
			Application.showSystemError(this);
		}

		initTable();
		buildTable();
		initWidgets();
		doSignals();
	}
	
	public CustomersScreen(QWidget parent) {
		this("SELECT * FROM customers", parent);
	}
	
	private void doSignals() {
		newBtn.clicked.connect(this, "showNewCustDlg()");
		prevBtn.clicked.connect(this, "showPrevious()");
		nextBtn.clicked.connect(this, "showNext()");
	}
	
	private void showNext() {
		currentPage++;
		buildSQL();

		try {
			fetchRows();
			if (rows.size() <= 0) {
				currentPage--;
				buildSQL();
				fetchRows();
				QMessageBox.information(this, "No more results", "No more results to show");
			}
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}
		
		buildTable();
	}
	
	private void showPrevious() {
		if (currentPage == 1) {
			return;
		}
		
		currentPage--;
		buildSQL();

		try {
			fetchRows();
			if (rows.size() <= 0) {
				currentPage++;
				buildSQL();
				fetchRows();
				QMessageBox.information(this, "No more results", "No more results to show");
			}
		} catch (SQLException e) {
			Application.showSystemError(this);
			return;
		}
		
		buildTable();
	}
	
	private void showNewCustDlg() {
		Customer c = NewCustomerScreen.createCustomer(this);
		
		if (c != null & rows.size() < resultsPerPage) {
			rows.add(c);
			buildTable();
		}
	}
	
	private void buildSQL() {
		int xLim = (currentPage - 1) * resultsPerPage;
		int yLim = currentPage * resultsPerPage;

		sql = originalSQL + " LIMIT " + xLim + ", " + yLim;
	}
	
	private void initWidgets() {
		navLay.addWidget(newBtn);
		navLay.addWidget(prevBtn);
		navLay.addWidget(nextBtn);

		navWid.setLayout(navLay);
		mainLay.addWidget(navWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(mainTable);

		mainWid.setLayout(mainLay);

		this.setCentralWidget(mainWid);
	}
	
	private void initTable() {
		mainTable.setColumnCount(headers.length);
		mainTable.setHorizontalHeaderLabels(Arrays.asList(headers));

		mainTable.setEditTriggers(EditTrigger.NoEditTriggers);
		mainTable.setSelectionMode(SelectionMode.NoSelection);
		mainTable.setAlternatingRowColors(true);

		mainTable.horizontalHeader().setResizeMode(ResizeMode.Stretch);

		// Init right-click menu
		mainTable.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
		mainTable.customContextMenuRequested.connect(this,
				"provideMenu(QPoint)");
		
		mainTable.setMinimumWidth(mainTable.horizontalHeader().length());
	}
	
	private void provideMenu(QPoint p) {
		QMenu menu = new QMenu(this);

		QTableWidgetItem cell = mainTable.itemAt(p);
		QAction editAct = new QAction("Edit " + mainTable.item(cell.row(), 0).text(), this);
		QAction delAct = new QAction("Delete " + mainTable.item(cell.row(), 0).text(), this);
		
		UserRole role = Application.getInstance().getLoggedInStaff().getRole();
		
		if (role != UserRole.ADMIN) {
			editAct.setEnabled(false);
			delAct.setEnabled(false);
		}
		
		editAct.triggered.connect(this, "edit()");
		delAct.triggered.connect(this, "delete()");

		menu.addAction(delAct);
		menu.addSeparator();
		menu.addAction(editAct);

		menu.popup(QCursor.pos());
	}
	
	private void edit() {
		QTableWidgetItem cell = mainTable.currentItem();
		Customer c = rows.get(cell.row());
		
		Customer updatedCustomer = EditCustomerScreen.edit(c, this);

		if (updatedCustomer != null) {
			rows.set(cell.row(), updatedCustomer);
			buildTable();
		}
	}
	
	private void delete() {
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();

		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);

		QTableWidgetItem cell = mainTable.currentItem();

		QMessageBox.StandardButton ret = QMessageBox.warning(this, tr("Delete Customer"),
				tr("Are you sure you want to delete " +
						mainTable.item(cell.row(), 0).text() + "?"),
						buttons, QMessageBox.StandardButton.No);

		if (ret.equals(QMessageBox.StandardButton.Yes)) {
			Customer c = rows.get(cell.row());
			
			try {
				if (Customer.delete(c.getCustomerID())) {
					rows.remove(cell.row());
					buildTable();
				}
			} catch (BilleException e) {
				QMessageBox.critical(this, "Error", e.getMessage());
			} catch (SQLException e) {
				Application.showSystemError(this);
			}
		}
	}
	
	private void fetchRows() throws SQLException {
		rows = Customer.search(sql);
	}
	
	private void buildTable() {
		mainTable.clearContents();

		mainTable.setRowCount(rows.size());

		int i = 0;
		for (Customer c : rows) {

			QTableWidgetItem firstName = new QTableWidgetItem(c.getFirstName());
			QTableWidgetItem lastName = new QTableWidgetItem(c.getLastName());
			QTableWidgetItem phoneNo = new QTableWidgetItem(c.getPhoneNum());
			QTableWidgetItem sex = new QTableWidgetItem(Character.toString(c
					.getSex()));
			QTableWidgetItem age = new QTableWidgetItem(Integer.toString(c
					.getAge()));

			mainTable.setItem(i, 0, firstName);
			mainTable.setItem(i, 1, lastName);
			mainTable.setItem(i, 2, phoneNo);
			mainTable.setItem(i, 3, age);
			mainTable.setItem(i, 4, sex);

			i++;
		}
		
	}
	
	private String originalSQL;
	private String sql;
	private int currentPage = 1, resultsPerPage = 30;
	private ArrayList<Customer> rows;

	private QWidget mainWid = new QWidget(this);
	private QVBoxLayout mainLay = new QVBoxLayout(mainWid);
	
	private QWidget navWid = new QWidget(this);
	private QHBoxLayout navLay = new QHBoxLayout(navWid);
	private RectButton newBtn = new RectButton(navWid, "New", GradientColor.GREEN);
	private RectButton prevBtn = new RectButton(navWid, "Previous", GradientColor.BLUE);
	private RectButton nextBtn = new RectButton(navWid, "Next", GradientColor.BLUE);
	
	private QTableWidget mainTable = new QTableWidget(this);
	private String[] headers = {"First Name", "Last Name", "Phone Num.", "Age", "Sex"};
}