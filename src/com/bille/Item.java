package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.ItemCodeExistsException;
import com.bille.exceptions.ItemIDExistsException;
import com.bille.exceptions.NoSuchCategoryIDException;
import com.bille.exceptions.NoSuchItemIDException;

public class Item {
	
	public static final int INVALID_ID = -1, INVALID_NAME = -2,
			INVALID_CODE = -3, INVALID_CAT = -3, INVALID_PRICE = -4;
	
	private String itemID;
	private String name;
	private String code;
	
	private String category;
	private String desc;
	private float price;
	private boolean isAvailable;
	
	public Item() {
		
	}
	
	public Item(String id, String name, String code, String category, String desc, float price, boolean isAvailable) {
		this.itemID = id;
		this.name = name;
		this.code = code;
		this.category = category;
		this.desc = desc;
		this.price = price;
		this.isAvailable = isAvailable;
	}
	
	// Construct from DB using item code
	
	public Item(String code) throws BilleException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM items WHERE code='" + code + "'";
		
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			throw new NoSuchItemIDException();
		}
		
		this.itemID = rs.getString("item_id");
		this.name = rs.getString("name");
		this.code = rs.getString("code");
		this.category = rs.getString("category");
		this.desc = rs.getString("description");
		this.price = rs.getFloat("price");
		this.isAvailable = rs.getBoolean("is_available");
	}
	
	public static Item getInstanceByID(String id) throws BilleException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM items WHERE item_id='" + id + "'";
		
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			throw new NoSuchItemIDException();
		}
		
		Item i = new Item();
		
		i.itemID = rs.getString("item_id");
		i.name = rs.getString("name");
		i.code = rs.getString("code");
		i.category = rs.getString("category");
		i.desc = rs.getString("description");
		i.price = rs.getFloat("price");
		i.isAvailable = rs.getBoolean("is_available");
		
		return i;
	}
	
	// Getters
	
	public String getItemID() {
		return itemID;
	}
	
	public String getName() {
		return name;
	}
	
	public String getCode() {
		return code;
	}
	
	public String getCategory() {
		return category;
	}
	
	public String getDesc() {
		return desc;
	}
	
	public float getPrice() {
		return price;
	}

	public boolean isAvailable() {
		return isAvailable;
	}
	
	// Setters
	
	public void setItemID(String itemID) {
		this.itemID = itemID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setCategory(String catID) {
		this.category = catID;
	}
	
	public void setDesc(String desc) {
		this.desc = desc;
	}

	public void setPrice(float price) {
		this.price = price;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}
	
	// Validation
	
	private int validate() {
		if (itemID == null || itemID.equals("")) {
			return INVALID_ID;
		}
		if (name == null || name.equals("")) {
			return INVALID_NAME;
		}
		if (category == null || category.equals("")) {
			return INVALID_CAT;
		}
		if (code == null || code.equals("")) {
			return INVALID_CODE;
		}
		if (desc == null) {
			desc = "";
		}
		if (price < 0) {
			return INVALID_PRICE;
		}
		
		return 0;
	}
	
	// DB functions
	
	public int createNew() throws BilleException, SQLException {
		
		int ret = validate();
		if (ret != 0) {
			return ret;
		}
		
		if (checkItemExistsByID(itemID)) {
			throw new ItemIDExistsException();
		}
		if (checkItemExistsByCode(code)) {
			throw new ItemCodeExistsException();
		}
		if (!Category.checkCategoryExistsByID(category)) {
			throw new NoSuchCategoryIDException();
		}
		
		MainDB db = Application.getInstance().getMainDB();
		
		String sql = "INSERT INTO items VALUES (";
		sql += "'" + itemID + "', ";
		sql += "'" + name + "', ";
		sql += "'" + code + "', ";
		sql += "'" + category + "', ";
		sql += "'" + desc + "', ";
		sql += price + ", ";
		sql += (isAvailable ? "1" : "0");
		sql += ")";
		
		if (db.executeSQL(sql)) {
			return 0;
		}
		
		throw new DatabaseException();
	}
	
	public int update() throws BilleException, SQLException {
		
		if (RestoOrder.isAnyOrderActive()) {
			throw new ActiveOrderPresentException();
		}
		
		int ret = validate();
		if (ret != 0) {
			return ret;
		}
		
		if (!checkItemExistsByID(itemID)) {
			throw new NoSuchItemIDException();
		}
		String itmID = getItemIDByCode(code);
		if (itmID != null && !itmID.equals(itemID)) {
			throw new ItemCodeExistsException();
		}
		if (!Category.checkCategoryExistsByID(category)){
			throw new NoSuchCategoryIDException();
		}
		
		MainDB db = Application.getInstance().getMainDB();
		
		String sql = "UPDATE items SET ";
		sql += "name='" + name + "', ";
		sql += "code='" + code + "', ";
		sql += "category='" + category + "', ";
		sql += "description='" + desc + "', ";
		sql += "price=" + price + ", ";
		sql += "is_available=" + (isAvailable ? "1" : "0");
		sql += " WHERE item_id='" + itemID + "'";
		
		if (db.executeSQL(sql)) {
			return 0;
		}
		
		throw new DatabaseException();
 	}
	
	public static boolean checkItemExistsByCode(String code) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT item_id FROM items WHERE code='" + code + "'";
		
		ResultSet rs = db.fetchRows(sql);
		
		if (rs.next()) {
			return true;
		}
		
		return false;
	}
	
	public static boolean checkItemExistsByID(String itemID) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT item_id FROM items WHERE item_id='" + itemID + "'";
		
		ResultSet rs = db.fetchRows(sql);
		
		if (rs.next()) {
			return true;
		}
		
		return false;
	}
	
	public static String getItemIDByCode(String code) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT item_id FROM items WHERE code='" + code + "'";
		
		ResultSet rs = db.fetchRows(sql);
		
		if (!rs.next()) {
			return null;
		}
		
		return rs.getString("item_id");
	}
	
	public static boolean delete(String itemID) throws BilleException, SQLException {
		
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}
		
		MainDB db = Application.getInstance().getMainDB();
		String sql = "DELETE FROM items WHERE item_id='" + itemID + "'";
		
		return db.executeSQL(sql);
	}
	
	// Search function
	
	public static ArrayList<Item> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);
		
		ArrayList<Item> itemsList = new ArrayList<Item>();
		
		while (rs.next()) {
			String id = rs.getString("item_id");
			String name = rs.getString("name");
			String code = rs.getString("code");
			String category = rs.getString("category");
			String desc = rs.getString("description");
			float price = rs.getFloat("price");
			boolean isAvailable = rs.getBoolean("is_available");
			
			Item i = new Item(id, name, code, category, desc, price, isAvailable);
			itemsList.add(i);
		}
		
		return itemsList;
	}

}
