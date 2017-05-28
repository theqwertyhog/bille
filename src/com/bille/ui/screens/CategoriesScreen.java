package com.bille.ui.screens;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;

import com.bille.Application;
import com.bille.Category;
import com.bille.Customer;
import com.bille.UserRole;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.RectButton;
import com.trolltech.qt.core.QPoint;
import com.trolltech.qt.core.Qt;
import com.trolltech.qt.core.Qt.Alignment;
import com.trolltech.qt.core.Qt.AlignmentFlag;
import com.trolltech.qt.core.Qt.SortOrder;
import com.trolltech.qt.core.Qt.WindowModality;
import com.trolltech.qt.gui.*;
import com.trolltech.qt.gui.QAbstractItemView.EditTrigger;
import com.trolltech.qt.gui.QHeaderView.ResizeMode;

public class CategoriesScreen extends QDialog {

	public CategoriesScreen(String sql, QWidget parent) {
		super(parent);
		this.originalSQL = sql;
		this.sql = sql;

		this.setWindowTitle("Categories");
		
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

	public CategoriesScreen(QWidget parent) {
		this("SELECT * FROM categories", parent);
	}
	
	private void doSignals() {
		newBtn.clicked.connect(this, "createNewCat()");
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
	
	private void createNewCat() {
		Category c = NewCategoryScreen.createCat(this);
		
		if (c != null & rows.size() < resultsPerPage) {
			rows.add(c);
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
		Category c = rows.get(cell.row());
		
		Category updatedCat = EditCategoryScreen.edit(c, this);

		if (updatedCat != null) {
			rows.set(cell.row(), updatedCat);
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
			Category c = rows.get(cell.row());
			
			try {
				if (Category.delete(c.getCatID())) {
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

		sql = originalSQL + " WHERE category_id != '0' LIMIT " + xLim + ", " + yLim;
	}

	private void fetchRows() throws SQLException {
		rows = Category.search(sql);
	}
	
	private void buildTable() {
		mainTable.clearContents();

		mainTable.setRowCount(rows.size());

		int i = 0;
		for (Category c : rows) {

			QTableWidgetItem name = new QTableWidgetItem();
			name.setData(0, c.getName());

			mainTable.setItem(i, 0, name);
			i++;
		}

	}

	private String originalSQL;
	private String sql;
	private int currentPage = 1, resultsPerPage = 30;
	private ArrayList<Category> rows;

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
	private String[] headers = { "Name" };

}
