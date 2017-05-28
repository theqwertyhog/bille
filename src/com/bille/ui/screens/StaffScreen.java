package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.bille.Application;
import com.bille.Staff;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.RectButton;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

public class StaffScreen extends QMainWindow {

	public StaffScreen(String sql, QWidget parent) {
		super(parent);
		this.originalSQL = sql;
		this.sql = sql;
		this.setWindowTitle("Staff");

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

	public StaffScreen(QWidget parent) {
		this("SELECT * FROM staff", parent);
	}

	private void doSignals() {
		newBtn.clicked.connect(this, "showNewStaffDlg()");
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
	
	private void showNewStaffDlg() {
		Staff s = NewStaffScreen.createStaff(this);
		if (s != null & rows.size() < resultsPerPage) {
			rows.add(s);
			buildTable();
		}
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

	private void buildTable() {
		mainTable.clearContents();

		mainTable.setRowCount(rows.size());

		int i = 0;
		for (Staff s : rows) {

			QTableWidgetItem firstName = new QTableWidgetItem(s.getFirstName());
			QTableWidgetItem lastName = new QTableWidgetItem(s.getLastName());
			QTableWidgetItem phoneNo = new QTableWidgetItem(s.getPhoneNum());
			QTableWidgetItem sex = new QTableWidgetItem(Character.toString(s
					.getSex()));
			QTableWidgetItem age = new QTableWidgetItem(Integer.toString(s
					.getAge()));
			QTableWidgetItem address = new QTableWidgetItem(s.getAddress());
			QTableWidgetItem role = new QTableWidgetItem(s.getRole().toString());
			QTableWidgetItem userID = new QTableWidgetItem(s.getUserID());
			QTableWidgetItem isActive = new QTableWidgetItem(Boolean.toString(s
					.isActive()));

			mainTable.setItem(i, 0, firstName);
			mainTable.setItem(i, 1, lastName);
			mainTable.setItem(i, 2, phoneNo);
			mainTable.setItem(i, 3, age);
			mainTable.setItem(i, 4, sex);
			mainTable.setItem(i, 5, address);
			mainTable.setItem(i, 6, role);
			mainTable.setItem(i, 7, userID);
			mainTable.setItem(i, 8, isActive);

			i++;
		}

	}

	private void provideMenu(QPoint p) {
		QMenu menu = new QMenu(mainTable);

		QTableWidgetItem cell = mainTable.itemAt(p);

		QAction editAct = new QAction("Edit "
				+ mainTable.item(cell.row(), 0).text(), this);
		QAction changePassAct = new QAction("Change password for "
				+ mainTable.item(cell.row(), 0).text(), this);
		QAction delAct = new QAction("Delete "
				+ mainTable.item(cell.row(), 0).text(), this);

		UserRole role = Application.getInstance().getLoggedInStaff().getRole();

		if (role != UserRole.ADMIN) {
			editAct.setEnabled(false);
			changePassAct.setEnabled(false);
			delAct.setEnabled(false);
		}

		editAct.triggered.connect(this, "edit()");
		changePassAct.triggered.connect(this, "changePassword()");
		delAct.triggered.connect(this, "delete()");

		menu.addAction(editAct);
		menu.addSeparator();
		menu.addAction(changePassAct);
		menu.addSeparator();
		menu.addAction(delAct);

		menu.popup(QCursor.pos());
	}

	private void edit() {
		QTableWidgetItem cell = mainTable.currentItem();
		Staff s = rows.get(cell.row());

		Staff updatedStaff = EditStaffScreen.edit(s, this);

		if (updatedStaff != null) {
			rows.set(cell.row(), updatedStaff);
			buildTable();
		}
	}

	private void changePassword() {
		QTableWidgetItem cell = mainTable.currentItem();
		Staff s = rows.get(cell.row());
		
		if (UpdatePasswordScreen.updatePassword(s.getUserID(), this)) {
			QMessageBox.information(this, "Update sucessful", "Password changed");
		}
	}

	private void delete() {
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();

		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);

		QTableWidgetItem cell = mainTable.currentItem();

		QMessageBox.StandardButton ret = QMessageBox.warning(this, tr("Delete Staff"),
				tr("Are you sure you want to delete " +
						mainTable.item(cell.row(), 0).text() + "?"),
						buttons, QMessageBox.StandardButton.No);

		if (ret.equals(QMessageBox.StandardButton.Yes)) {
			Staff s = rows.get(cell.row());
			
			try {
				if (Staff.delete(s.getStaffID())) {
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

	private void buildSQL() {
		int xLim = (currentPage - 1) * resultsPerPage;
		int yLim = currentPage * resultsPerPage;

		sql = originalSQL + " LIMIT " + xLim + ", " + yLim;
	}

	private void fetchRows() throws SQLException {
		rows = Staff.search(sql);
	}

	private String originalSQL;
	private String sql;
	private int currentPage = 1, resultsPerPage = 30;
	private ArrayList<Staff> rows;

	private QWidget mainWid = new QWidget(this);
	private QVBoxLayout mainLay = new QVBoxLayout(mainWid);

	private QWidget navWid = new QWidget(mainWid);
	private QHBoxLayout navLay = new QHBoxLayout(navWid);
	private RectButton newBtn = new RectButton(navWid, "New",
			GradientColor.GREEN);
	private RectButton prevBtn = new RectButton(navWid, "Previous",
			GradientColor.BLUE);
	private RectButton nextBtn = new RectButton(navWid, "Next",
			GradientColor.BLUE);

	private QTableWidget mainTable = new QTableWidget(mainWid);
	private String[] headers = { "First Name", "Last Name", "Phone No.", "Age",
			"Sex", "Address", "Role", "User ID", "Is active?" };
}