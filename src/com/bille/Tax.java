package com.bille;

import java.sql.SQLException;
import java.sql.ResultSet;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchTaxIDException;
import com.bille.exceptions.TaxIDExistsException;
import com.bille.exceptions.TaxNameExistsException;

public class Tax {

	public static final int INVALID_ID = -1, INVALID_NAME = -2,
			INVALID_VALUE = -3;

	private MainDB db = Application.getInstance().getMainDB();

	private String id;
	private String name;
	private float value;
	private boolean isEnabled;

	public Tax() {

	}

	public Tax(String id, String name, float value, boolean isEnabled) {
		this.id = id;
		this.name = name;
		this.value = value;
		this.isEnabled = isEnabled;
	}

	public Tax(String id) throws BilleException, SQLException {
		String sql = "SELECT * FROM taxes WHERE tax_id='" + id + "'";
		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchTaxIDException();
		}

		this.id = id;
		this.name = rs.getString("name");
		this.value = rs.getFloat("value");
		this.isEnabled = rs.getBoolean("enabled");
	}

	// Get error messages

	public static String getErrMsg(int errCode) {
		return Integer.toString(errCode);
	}

	// Getters and setters

	public String getTaxID() {
		return id;
	}

	public void setTaxID(String id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public float getValue() {
		return value;
	}

	public void setValue(float value) {
		this.value = value;
	}

	public boolean isEnabled() {
		return isEnabled;
	}

	public void setEnabled(boolean isEnabled) {
		this.isEnabled = isEnabled;
	}

	// Validation

	private int validate() {
		if (id == null | id.equals("")) {
			return INVALID_ID;
		}
		if (name == null | name.equals("")) {
			return INVALID_NAME;
		}
		if (value < 0) {
			return INVALID_VALUE;
		}

		return 0;
	}

	// DB functions

	public int createNew() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (checkTaxExists(id)) {
			throw new TaxIDExistsException();
		}
		String tID = getTaxIDByName(name);
		if (tID != null && !tID.equals(id)) {
			throw new TaxNameExistsException();
		}

		String sql = "INSERT INTO taxes VALUES (";
		sql += "'" + id + "', ";
		sql += "'" + name + "', ";
		sql += value + ", ";
		sql += (isEnabled ? "1" : "0");
		sql += ")";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public int update() throws BilleException, SQLException {
		
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (!checkTaxExists(id)) {
			throw new NoSuchTaxIDException();
		}
		String tID = getTaxIDByName(name);
		if (tID != null && !tID.equals(id)) {
			throw new TaxNameExistsException();
		}

		String sql = "UPDATE taxes SET ";
		sql += "name='" + name + "', ";
		sql += "value=" + value + ", ";
		sql += "enabled=" + (isEnabled ? "1" : "0");
		sql += " WHERE tax_id='" + id + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public static boolean checkTaxExists(String id) throws SQLException {
		String sql = "SELECT tax_id FROM taxes WHERE tax_id='" + id + "'";
		MainDB db = Application.getInstance().getMainDB();

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return false;
		}

		return true;
	}
	
	public static String getTaxIDByName(String name) throws SQLException {
		String sql = "SELECT tax_id FROM taxes WHERE name='" + name + "'";
		MainDB db = Application.getInstance().getMainDB();
		
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			return null;
		}
		
		return rs.getString("tax_id");
	}

	public static boolean delete(String id) throws BilleException, SQLException {
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}

		String sql = "DELETE FROM taxes WHERE tax_id='" + id + "'";
		return Application.getInstance().getMainDB().executeSQL(sql);
	}

	// Search function

	public static ArrayList<Tax> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		ArrayList<Tax> taxList = new ArrayList<Tax>();

		while (rs.next()) {
			String id = rs.getString("tax_id");
			String name = rs.getString("name");
			float value = rs.getFloat("value");
			boolean isEnabled = rs.getBoolean("enabled");

			Tax t = new Tax(id, name, value, isEnabled);
			taxList.add(t);
		}

		return taxList;
	}

}
