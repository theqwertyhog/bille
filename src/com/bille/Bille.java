package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;

import com.bille.exceptions.*;

public class Bille {

	private MainDB mainDB;
	private LiteDB liteDB;

	private Store store;
	private Staff loggedInStaff;

	// Constructor

	public Bille() throws BilleException {
		Application.setSplashMessage("Loading preferences");
		liteDB = new LiteDB();
		if (!liteDB.init()) {
			throw new LocalDBException();
		}		
	}

	public void init() throws BilleException, SQLException {
		Application.setSplashMessage("Setting preferences");
		this.store = new Store();
		
		Application.setSplashMessage("Connecting to database");
		mainDB = new MainDB();
		if (!mainDB.init(store.getDBName(), store.getDBUser(),
				store.getDBPassword())) {
			throw new DatabaseException();
		}
	}

	// Getters

	public MainDB getMainDB() {
		return mainDB;
	}

	public LiteDB getLiteDB() {
		return liteDB;
	}

	public Store getStore() {
		return store;
	}

	public Staff getLoggedInStaff() {
		return loggedInStaff;
	}
	
	public void setLoggedInStaff(Staff s) {
		this.loggedInStaff = s;
	}

	// DB function

	public Tax[] getApplicableTaxes() throws SQLException {
		String sql = "SELECT * FROM taxes WHERE enabled=1";
		ArrayList<Tax> taxes = Tax.search(sql);

		return (Tax[]) taxes.toArray();
	}

}
