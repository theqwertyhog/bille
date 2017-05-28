package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.bille.Application;
import com.bille.Customer;
import com.bille.Tax;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.RectButton;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

public class TaxesScreen extends QMainWindow {
	
	public TaxesScreen(String sql, QWidget parent) {
		super(parent);
		this.originalSQL = sql;
		this.sql = sql;
		this.setWindowTitle("Taxes");

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
	
	public TaxesScreen(QWidget parent) {
		this("SELECT * FROM taxes", parent);
	}
	
	private void doSignals() {
		newBtn.clicked.connect(this, "showNewTaxDlg()");
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
	
	private void fetchRows() throws SQLException {
		rows = Tax.search(sql);
	}
	
	private void showNewTaxDlg() {
		Tax t = NewTaxScreen.createTax(this);
		
		if (t != null & rows.size() < resultsPerPage) {
			rows.add(t);
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
		for (Tax t : rows) {

			QTableWidgetItem name = new QTableWidgetItem(t.getName());
			QTableWidgetItem value = new QTableWidgetItem(Float.toString(t.getValue()));
			QTableWidgetItem isEnabled = new QTableWidgetItem(Boolean.toString(t.isEnabled()));

			mainTable.setItem(i, 0, name);
			mainTable.setItem(i, 1, value);
			mainTable.setItem(i, 2, isEnabled);

			i++;
		}

	}

	private void provideMenu(QPoint p) {
		QMenu menu = new QMenu(this);

		QTableWidgetItem cell = mainTable.itemAt(p);
		QAction editAct = new QAction("Edit "
				+ mainTable.item(cell.row(), 0).text(), this);
		QAction delAct = new QAction("Delete "
				+ mainTable.item(cell.row(), 0).text(), this);

		UserRole role = Application.getInstance().getLoggedInStaff().getRole();

		if (role != UserRole.ADMIN) {
			editAct.setEnabled(false);
			delAct.setEnabled(false);
		}

		editAct.triggered.connect(this, "edit()");
		delAct.triggered.connect(this, "delete()");

		menu.addAction(editAct);
		menu.addSeparator();
		menu.addAction(delAct);

		menu.popup(QCursor.pos());
	}
	
	private void edit() {
		QTableWidgetItem cell = mainTable.currentItem();
		Tax t = rows.get(cell.row());
		
		Tax updatedTax = EditTaxScreen.edit(t, this);

		if (updatedTax != null) {
			rows.set(cell.row(), updatedTax);
			buildTable();
		}
	}
	
	private void delete() {
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();

		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);

		QTableWidgetItem cell = mainTable.currentItem();

		QMessageBox.StandardButton ret = QMessageBox.warning(this, tr("Delete Tax"),
				tr("Are you sure you want to delete " +
						mainTable.item(cell.row(), 0).text() + "?"),
						buttons, QMessageBox.StandardButton.No);

		if (ret.equals(QMessageBox.StandardButton.Yes)) {
			Tax t = rows.get(cell.row());
			
			try {
				if (Tax.delete(t.getTaxID())) {
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

	private String originalSQL;
	private String sql;
	private int currentPage = 1, resultsPerPage = 30;
	private ArrayList<Tax> rows;

	private QWidget mainWid = new QWidget(this);
	private QVBoxLayout mainLay = new QVBoxLayout(mainWid);

	private QWidget navWid = new QWidget(this);
	private QHBoxLayout navLay = new QHBoxLayout(navWid);
	private RectButton newBtn = new RectButton(navWid, "New",
			GradientColor.GREEN);
	private RectButton prevBtn = new RectButton(navWid, "Previous",
			GradientColor.BLUE);
	private RectButton nextBtn = new RectButton(navWid, "Next",
			GradientColor.BLUE);

	private QTableWidget mainTable = new QTableWidget(this);
	private String[] headers = { "Name", "Value", "Is enabled?"};
}