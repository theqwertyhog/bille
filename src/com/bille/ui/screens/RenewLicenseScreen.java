package com.bille.ui.screens;

import java.sql.SQLException;
import java.util.Date;

import com.bille.API;
import com.bille.Application;
import com.bille.License;
import com.bille.exceptions.BilleException;
import com.bille.ui.GradientColor;
import com.bille.widgets.LongButton;
import com.trolltech.qt.core.Qt.*;
import com.trolltech.qt.gui.*;

public class RenewLicenseScreen extends QFrame {
	
	public RenewLicenseScreen(QWidget parent) {
		super(parent);
		
		initWidgets();
		doSignals();
	}
	
	private void doSignals() {
		refreshBtn.clicked.connect(this, "refreshLic()");
	}
	
	// Slots
	
	private void refreshLic() {
		refreshBtn.setText("Loading...");
		refreshBtn.setEnabled(false);
		
		try {
			License lic = API.refreshLicense();
			Application.getInstance().getStore().updateLicense(lic);
			Application.getInstance().getStore().readData();
			
			Date expires = Application.getInstance().getStore().getExpires();
			Date now = new Date();
			
			if (now.compareTo(expires) < 0) {
				Application.showLoginScreen();
				return;
			} else {
				initInfoText();
			}
		} catch (BilleException e) {
			QMessageBox.critical(this, "Error", e.getMessage());
		} catch (SQLException e) {
			Application.showSystemError(this);
		}
		
		refreshBtn.setText("Refresh");
		refreshBtn.setEnabled(true);
	}
	
	// Widget functions
	
	private void initInfoText() {
		Date expired = Application.getInstance().getStore().getExpires();
		
		infoLab.setText("Your license has expired on <b>" + expired + "</b>. Please renew your subscription and "
				+ "click on 'Refresh'.<br /><center>For more information, visit "
				+ "<a href='http://pos.ellypsys.com/renew'>http://pos.ellypsys.com/renew</a>.</center>");
	}
	
	private void initWidgets() {
		infoLab.setStyleSheet("color: grey; font-size: 12px; text-align: center; margin-bottom: 10px;");
		initInfoText();
		
		mainLay.addWidget(infoLab, 0 , new Alignment(AlignmentFlag.AlignCenter));
		mainLay.addWidget(refreshBtn, 0 , new Alignment(AlignmentFlag.AlignCenter));
		this.setLayout(mainLay);
	}
	
	private QVBoxLayout mainLay = new QVBoxLayout(this);
	private QLabel infoLab = new QLabel(this);
	private LongButton refreshBtn = new LongButton(this, "Refresh", GradientColor.GREEN);

}
