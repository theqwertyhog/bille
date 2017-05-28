package com.bille.ui.screens;

import java.sql.*;

import com.bille.API;
import com.bille.Application;
import com.bille.License;
import com.bille.Store;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.bille.widgets.RectButton;
import com.bille.widgets.SmartPasswordBox;
import com.bille.widgets.SmartTextBox;
import com.bille.widgets.VLine;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class SettingsScreen extends QFrame {
	
	public static final int INVALID_STORE_NAME = -1, INVALID_ADDRESS = -2,
			INVALID_PHONE_ONE = -3, INVALID_WEBSITE = -4, INVALID_EMAIL = -4,
			INVALID_DB_NAME = -5, INVALID_DB_USER = -6, INVALID_DB_PASSWORD = -7;
	
	public static String getErrorMessage(int errCode) {
		return Integer.toString(errCode);
	}

	public SettingsScreen(QWidget parent) {
		super(parent);

		initWidgets();
		readFields();

		doSignals();
	}

	private void doSignals() {
		updateBtn.clicked.connect(this, "doUpdate()");
		refreshLicBtn.clicked.connect(this, "refreshLic()");
	}

	// Slots
	
	private void refreshLic() {
		refreshLicBtn.setText("Loading...");
		refreshLicBtn.setEnabled(false);
		
		try {
			License lic = API.refreshLicense();
			Application.getInstance().getStore().updateLicense(lic);
			Application.getInstance().getStore().readData();
			
			if (Application.hasExpired()) {
				this.disposeLater();
				Application.logout();
				Application.showRenewLicenseScreen();
				return;
			}
			
			readFields();
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
		} catch (SQLException e) {
			Application.showSystemError(this);
		}
		
		refreshLicBtn.setText("Refresh");
		refreshLicBtn.setEnabled(true);
	}

	private void doUpdate() {
		int ret = validate();
		
		if (ret != 0) {
			QMessageBox.critical(this, "Error", getErrorMessage(ret));
			return;
		}
		
		if (!testConnection()) {
			QMessageBox
					.critical(
							this,
							"Database Error",
							"Can't connect to the database!<br /><br />"
							+ "Please check the connection parameters and try again.");
			
			return;
		}
		
		String dbName = dbNameBox.getText();
		String dbUser = dbUserBox.getText();
		String dbPass = dbPasswdBox.getPassword();
		
		s.setDBName(dbName);
		s.setDBUser(dbUser);
		s.setDBPassword(dbPass);
		
		try {
			s.update();
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
			return;
		} catch (SQLException e) {
			e.printStackTrace();
			Application.showSystemError(this);
			return;
		}
		
		QMessageBox.information(this, "Success", "Update successful.<br /><br />"
				+ "Please restart application to apply effects.");
	}

	// Control functions
	
	private int validate() {
		String dbName = dbNameBox.getText();
		String dbUser = dbUserBox.getText();
		String dbPass = dbPasswdBox.getPassword();
		
		if (dbName == null || dbName.equals("")) {
			return INVALID_DB_NAME;
		}
		else if (dbUser == null || dbUser.equals("")) {
			return INVALID_DB_USER;
		}
		else if (dbPass == null || dbPass.equals("")) {
			return INVALID_DB_PASSWORD;
		}
		
		return 0;
	}

	private boolean testConnection() {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			Connection connection = DriverManager.getConnection(
					"jdbc:mysql://localhost/" + dbNameBox.getText(),
					dbUserBox.getText(), dbPasswdBox.getPassword());

			connection.close();
		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	private void readFields() {
		storeNameLab.setText("<b>Store name: </b>" + s.getName());
		addressLab.setText("<b>Address: </b>" + s.getAddress());
		phoneOneLab.setText("<b>Phone one: </b>" + s.getPhoneOne());
		phoneTwoLab.setText("<b>Phone two: </b>" + s.getPhoneTwo());
		phoneThreeLab.setText("<b>Phone three: </b>" + s.getPhoneThree());
		websiteLab.setText("<b>Website: </b>" + s.getWebsite());
		emailLab.setText("<b>email: </b>" + s.getEmail());
		expiresLab.setText("<b>Expires: </b>" + s.getExpires());

		dbNameBox.setText(s.getDBName());
		dbUserBox.setText(s.getDBUser());
		dbPasswdBox.setPassword(s.getDBPassword());
	}
	
	// Widget functions
	
	private void initLicenseWid() {
		licenseInfoLab.setProperty("is_heading", true);
		
		licenseLay.addWidget(licenseInfoLab);
		licenseLay.addWidget(storeNameLab);
		licenseLay.addWidget(addressLab);
		licenseLay.addWidget(phoneOneLab);
		licenseLay.addWidget(phoneTwoLab);
		licenseLay.addWidget(phoneThreeLab);
		licenseLay.addWidget(websiteLab);
		licenseLay.addWidget(emailLab);
		licenseLay.addWidget(expiresLab);
		licenseLay.addWidget(refreshLicBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		
		licenseWid.setLayout(licenseLay);
	}
	
	private void initDBWid() {
		dbSettingsLab.setProperty("is_heading", true);

		dbNameBox.setPlaceholder("Database name");
		dbUserBox.setPlaceholder("Database user");
		dbPasswdBox.setPlaceholder("Database password");

		dbLay.addWidget(dbSettingsLab, 0, new Alignment(AlignmentFlag.AlignCenter));
		dbLay.addWidget(dbNameBox);
		dbLay.addWidget(dbUserBox);
		dbLay.addWidget(dbPasswdBox);
		dbLay.addWidget(updateBtn, 0, new Alignment(AlignmentFlag.AlignCenter));
		
		dbWid.setLayout(dbLay);
	}

	private void initWidgets() {
		initLicenseWid();
		initDBWid();
		
		mainLay.addWidget(licenseWid);
		mainLay.addWidget(new VLine(this));
		mainLay.addWidget(dbWid);

		this.setLayout(mainLay);
	}

	private Store s = Application.getInstance().getStore();

	private QHBoxLayout mainLay = new QHBoxLayout(this);

	private QWidget licenseWid = new QWidget(this);
	private QVBoxLayout licenseLay = new QVBoxLayout(licenseWid);
	private QLabel licenseInfoLab = new QLabel("License Information", licenseWid);
	private QLabel storeNameLab = new QLabel(licenseWid);
	private QLabel addressLab = new QLabel(licenseWid);
	private QLabel phoneOneLab = new QLabel(licenseWid);
	private QLabel phoneTwoLab = new QLabel(licenseWid);
	private QLabel phoneThreeLab = new QLabel(licenseWid);
	private QLabel websiteLab = new QLabel(licenseWid);
	private QLabel emailLab = new QLabel(licenseWid);
	private QLabel expiresLab = new QLabel(licenseWid);
	private LongButton refreshLicBtn = new LongButton(licenseWid, "Refresh", GradientColor.BLUE);

	private QWidget dbWid = new QWidget(this);
	private QVBoxLayout dbLay = new QVBoxLayout(dbWid);
	private QLabel dbSettingsLab = new QLabel("Database Settings", dbWid);
	private SmartTextBox dbNameBox = new SmartTextBox(dbWid);
	private SmartTextBox dbUserBox = new SmartTextBox(dbWid);
	private SmartPasswordBox dbPasswdBox = new SmartPasswordBox(dbWid);
	private LongButton updateBtn = new LongButton(dbWid, "Update",
			GradientColor.GREEN);
}
