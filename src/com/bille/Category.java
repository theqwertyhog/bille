package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.*;

public class Category {
	
	public static final int INVALID_ID = -1;
	public static final int INVALID_NAME = -2;
	
	private MainDB db = Application.getInstance().getMainDB();
	
	private String catID;
	private String name;
	
	public Category() {
		
	}
	
	public Category(String catID, String name) {
		this.catID = catID;
		this.name = name;		
	}
	
	public Category(String catID) throws BilleException, SQLException {
		String sql = "SELECT * FROM categories WHERE category_id='" + catID + "'";
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			throw new NoSuchCategoryIDException();
		}
		
		this.catID = catID;
		this.name = rs.getString("name");
	}
	
	private void setDefaults(String catID) {
		this.catID = catID;
		this.name = "N/A";
	}
	
	// Getters and setters
	
	public String getCatID() {
		return catID;
	}
	
	public void setCatID(String catID) {
		this.catID = catID;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	// Validation
	
	private int validate() {
		if (catID == null | catID.equals("")) {
			return INVALID_ID;
		}
		if (name == null | name.equals("")) {
			return INVALID_NAME;
		}
		
		return 0;
	}
	
	// DB functions
	
	public int createNew() throws BilleException, SQLException {
		
		int ret = validate();
		if (ret != 0) {
			return ret;
		}
		
		if (checkCategoryExistsByID(catID)) {
			throw new CategoryIDExistsException();
		}
		if (checkCategoryExistsByName(name)) {
			throw new CategoryNameExistsException();
		}
		
		String sql = "INSERT INTO categories VALUES (";
		sql += "'" + catID + "', ";
		sql += "'" + name + "'";
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
		
		if (!checkCategoryExistsByID(catID)) {
			throw new NoSuchCategoryIDException();
		}
		String cID = getCatIDByName(name);
		if (cID != null && !cID.equals(catID)) {
			throw new CategoryNameExistsException();
		}
		
		String sql = "UPDATE categories SET ";
		sql += "name='" + name + "'";
		sql += " WHERE category_id='" + catID + "'";
		
		if (db.executeSQL(sql)) {
			return 0;
		}
		
		throw new DatabaseException();
	}
	
	public static boolean checkCategoryExistsByID(String catID) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT category_id FROM categories WHERE category_id='" + catID + "'";
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			return false;
		}
		
		return true;
	}
	
	public static boolean checkCategoryExistsByName(String name) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT category_id FROM categories WHERE name='" + name + "'";
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			return false;
		}
		
		return true;
	}
	
	public static String getCatIDByName(String name) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT category_id FROM categories WHERE name='" + name + "'";
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			return null;
		}
		
		return rs.getString("category_id");
	}
	
	public static boolean delete(String catID) throws BilleException, SQLException {
		
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}
		
		MainDB db = Application.getInstance().getMainDB();
		String sql = "DELETE FROM categories WHERE category_id='" + catID + "'";
		sql += "; UPDATE items SET category='' WHERE category='" + catID + "'";
		sql += "; UPDATE products SET category='' WHERE category='" + catID + "'";
		
		return db.executeSQL(sql);
	}
	
	// Search function
	
	public static ArrayList<Category> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		ArrayList<Category> catList = new ArrayList<Category>();

		while (rs.next()) {
			String id = rs.getString("category_id");
			String name = rs.getString("name");
			
			Category c = new Category(id, name);
			catList.add(c);
		}

		return catList;
	}
	
	public static ArrayList<Category> getAll() throws SQLException {
		return search("SELECT * FROM categories ORDER BY category_id");
	}
	
}
