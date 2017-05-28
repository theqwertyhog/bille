package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.Item;
import com.bille.Product;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QAbstractItemView.SelectionMode;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

public class ItemsScreen extends QDialog {

	public ItemsScreen(String sql, QWidget parent) {
		super(parent);
		this.sql = sql;
		this.originalSQL = sql;
		this.setWindowTitle("Items");

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

	public ItemsScreen(QWidget parent) {
		this("SELECT * FROM items", parent);
	}

	public static Item selectItem(String sql, QWidget parent) {
		ItemsScreen s = new ItemsScreen(sql, parent);

		s.cancelBtn.show();
		s.selectBtn.show();
		s.selectLay.addWidget(s.cancelBtn);
		s.selectLay.addWidget(s.selectBtn);
		s.selectWid.setLayout(s.selectLay);
		s.mainLay.addWidget(s.selectWid, 0, new Alignment(
				AlignmentFlag.AlignCenter));

		s.newBtn.hide();
		s.mainTable.setContextMenuPolicy(Qt.ContextMenuPolicy.NoContextMenu);

		s.cancelBtn.clicked.connect(s, "reject()");
		s.selectBtn.clicked.connect(s, "accept()");

		boolean ok = s.exec() == QDialog.DialogCode.Accepted.value();

		if (ok) {
			int selectedRow = s.mainTable.currentRow();
			if (selectedRow > -1) {
				return s.rows.get(selectedRow);
			}
		}

		return null;
	}

	private void doSignals() {
		newBtn.clicked.connect(this, "createNewItem()");
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

	private void createNewItem() {
		Item i = NewItemScreen.createItem(this);

		if (i != null & rows.size() < resultsPerPage) {
			rows.add(i);
			buildTable();
		}
	}

	private void initWidgets() {
		cancelBtn.hide();
		selectBtn.hide();

		navLay.addWidget(newBtn);
		navLay.addWidget(prevBtn);
		navLay.addWidget(nextBtn);

		navWid.setLayout(navLay);

		mainLay.addWidget(navWid, 0, new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(mainTable);

		this.setLayout(mainLay);
	}

	private void initTable() {
		mainTable.setColumnCount(headers.length);
		mainTable.setHorizontalHeaderLabels(Arrays.asList(headers));

		mainTable.setEditTriggers(EditTrigger.NoEditTriggers);
		mainTable.setAlternatingRowColors(true);
		mainTable.horizontalHeader().setResizeMode(ResizeMode.Stretch);
		mainTable.setSortingEnabled(true);

		// Init right-click menu
		mainTable.setContextMenuPolicy(Qt.ContextMenuPolicy.CustomContextMenu);
		mainTable.customContextMenuRequested.connect(this,
				"provideMenu(QPoint)");

		mainTable.setMinimumWidth(mainTable.horizontalHeader().length());
	}

	private void provideMenu(QPoint p) {
		QMenu menu = new QMenu(this);

		QTableWidgetItem cell = mainTable.itemAt(p);

		if (cell != null) {
			QAction editAct = new QAction("Edit "
					+ mainTable.item(cell.row(), 0).text(), this);
			QAction delAct = new QAction("Delete "
					+ mainTable.item(cell.row(), 0).text(), this);

			UserRole role = Application.getInstance().getLoggedInStaff()
					.getRole();

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
	}

	private void edit() {
		QTableWidgetItem cell = mainTable.currentItem();

		if (cell == null) {
			return;
		}

		Item i = rows.get(cell.row());

		Item updatedItem = EditItemScreen.edit(i, this);

		if (updatedItem != null) {
			rows.set(cell.row(), updatedItem);
			buildTable();
		}
	}

	private void delete() {
		QMessageBox.StandardButtons buttons = new QMessageBox.StandardButtons();

		buttons.set(QMessageBox.StandardButton.Yes);
		buttons.set(QMessageBox.StandardButton.No);

		QTableWidgetItem cell = mainTable.currentItem();

		QMessageBox.StandardButton ret = QMessageBox.warning(this,
				tr("Delete Item"), tr("Are you sure you want to delete "
						+ mainTable.item(cell.row(), 0).text() + "?"), buttons,
				QMessageBox.StandardButton.No);

		if (ret.equals(QMessageBox.StandardButton.Yes)) {
			Item i = rows.get(cell.row());

			try {
				if (Item.delete(i.getItemID())) {
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
		rows = Item.search(sql);
	}

	private void buildTable() {
		mainTable.clearContents();

		mainTable.setRowCount(rows.size());

		int i = 0;
		for (Item itm : rows) {
			QTableWidgetItem name = new QTableWidgetItem(itm.getName());
			QTableWidgetItem code = new QTableWidgetItem(itm.getCode());

			Category c;

			try {
				c = new Category(itm.getCategory());
			} catch (Exception e) {
				c = null;
			}

			QTableWidgetItem catName;
			if (c == null) {
				catName = new QTableWidgetItem("N/A");
			} else {
				catName = new QTableWidgetItem(c.getName());
			}

			QTableWidgetItem desc = new QTableWidgetItem(itm.getDesc());
			QTableWidgetItem price = new QTableWidgetItem(Float.toString(itm
					.getPrice()));
			QTableWidgetItem isAvailable = new QTableWidgetItem(
					Boolean.toString(itm.isAvailable()));

			mainTable.setItem(i, 0, name);
			mainTable.setItem(i, 1, code);
			mainTable.setItem(i, 2, catName);
			mainTable.setItem(i, 3, desc);
			mainTable.setItem(i, 4, price);
			mainTable.setItem(i, 5, isAvailable);

			i++;
		}

	}

	private String originalSQL;
	private String sql;
	private int currentPage = 1, resultsPerPage = 30;
	private ArrayList<Item> rows;

	private QVBoxLayout mainLay = new QVBoxLayout(this);

	private QWidget navWid = new QWidget(this);
	private QHBoxLayout navLay = new QHBoxLayout(navWid);
	private RectButton newBtn = new RectButton(navWid, "New",
			GradientColor.GREEN);
	private RectButton prevBtn = new RectButton(navWid, "Previous",
			GradientColor.BLUE);
	private RectButton nextBtn = new RectButton(navWid, "Next",
			GradientColor.BLUE);

	private QTableWidget mainTable = new QTableWidget(this);

	private QWidget selectWid = new QWidget(this);
	private QHBoxLayout selectLay = new QHBoxLayout(selectWid);
	private LongButton cancelBtn = new LongButton(selectWid, "Cancel",
			GradientColor.RED);
	private LongButton selectBtn = new LongButton(selectWid, "Select",
			GradientColor.GREEN);

	private String[] headers = { "Name", "Code", "Category", "Description",
			"Price", "Is available?" };
}
