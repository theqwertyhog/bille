package com.bille.ui.screens;

import java.sql.SQLException;

import com.bille.Application;
import com.bille.Staff;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.ui.*;
import com.bille.widgets.*;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class LoginScreen extends QFrame {
	
	public static final int INVALID_USER_ID = -1, INVALID_PASSWORD = -2;
	
	public LoginScreen(QWidget parent) {
		super(parent);
		this.setObjectName("LoginScreen");
		this.setFrameShadow(Shadow.Raised);
		
		initWidgets();
		doSignals();
	}

	// TODO: For testing only. Remove afterwards.
	
	public void hardLogin() {
		userIDBox.setText("admin");
		passwdBox.setPassword("1234");
		login();
	}
	
	private void doSignals() {
		loginBtn.clicked.connect(this, "login()");
	}
	
	private int validate() {
		String userID = userIDBox.getText();
		String passwd = passwdBox.getPassword();
		
		if (userID.equals("") | userID.equals(null)) {
			return INVALID_USER_ID;
		}
		if (passwd.equals("") | passwd.equals(null)) {
			return INVALID_PASSWORD;
		}
		
		return 0;
	}
	
	private void login() {
		int ret = validate();
		
		if (ret == INVALID_USER_ID) {
			QMessageBox.critical(this, "Login error", "Invalid user ID");
			return;
		}
		if (ret == INVALID_PASSWORD) {
			QMessageBox.critical(this, "Login error", "Invalid password");
			return;
		}
		
		String userID = userIDBox.getText();
		String passwd = passwdBox.getPassword();
		
		try {			
			
			if (!Staff.readPassword(userID).equals(passwd)) {
				QMessageBox.critical(this, "Login error", "Wrong password");
				return;
			}
			
			Application.getInstance().setLoggedInStaff(new Staff(userID));
			new Application().showHomeScreen();
			Application.showMenuBar();
		} catch (NoSuchStaffIDException e) {
			QMessageBox.critical(this, "Login error", "No such staff found");
		} catch (SQLException e) {
			Application.showSystemError(this);
		}
		
	}
	
	private void initWidgets() {
		loginLab.setStyleSheet("color: #60abf8; font-weight: bold;");
		userIDBox.setPlaceholder("User ID");
		passwdBox.setPlaceholder("Password");
				
		mainLayout.addWidget(loginLab, 0, new Alignment(AlignmentFlag.AlignHCenter, AlignmentFlag.AlignVCenter));
		mainLayout.addWidget(userIDBox, 0, new Alignment(AlignmentFlag.AlignHCenter, AlignmentFlag.AlignVCenter));
		mainLayout.addWidget(passwdBox, 0, new Alignment(AlignmentFlag.AlignHCenter, AlignmentFlag.AlignVCenter));
		mainLayout.addWidget(loginBtn, 0, new Alignment(AlignmentFlag.AlignHCenter, AlignmentFlag.AlignVCenter));
	
		this.setLayout(mainLayout);
	}
	
	private QVBoxLayout mainLayout = new QVBoxLayout(this);
	private QLabel loginLab = new QLabel("Please log in");
	private SmartTextBox userIDBox = new SmartTextBox(this);
	private SmartPasswordBox passwdBox = new SmartPasswordBox(this);
	private LongButton loginBtn = new LongButton(this, "Login", GradientColor.BLUE);

}
