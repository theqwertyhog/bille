package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchCategoryIDException;
import com.bille.exceptions.NoSuchProductIDException;
import com.bille.exceptions.ProductCodeExistsException;
import com.bille.exceptions.ProductIDExistsException;

public class Product {

	public static final int INVALID_ID = -1, INVALID_CAT = -2,
			INVALID_NAME = -3, INVALID_CODE = -4, INVALID_QUANT = -5,
			INVALID_UNIT = -6, INVALID_PPU = -7;

	private String productID;
	private String category;
	private String name;
	private String code;

	private float quantity;
	private Unit unit;
	private float pricePerUnit;
	private boolean isAvailable;
	
	public Product() {
		
	}

	public Product(String id, String name, String category, String code,
			float quantity, Unit unit, float pricePerUnit, boolean isAvailable) {
		this.productID = id;
		this.name = name;
		this.category = category;
		this.code = code;
		this.quantity = quantity;
		this.unit = unit;
		this.pricePerUnit = pricePerUnit;
		this.isAvailable = isAvailable;
	}

	// Construct from DB using product code

	public Product(String code) throws BilleException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM products WHERE code='" + code + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchProductIDException();
		}

		this.productID = rs.getString("product_id");
		this.category = rs.getString("category");
		this.name = rs.getString("name");
		this.code = rs.getString("code");
		this.quantity = rs.getFloat("quantity");
		this.unit = Unit.valueOf(rs.getString("unit"));
		this.pricePerUnit = rs.getFloat("price_per_unit");
		this.isAvailable = rs.getBoolean("is_available");
	}
	
	public static Product getInstanceByID(String id) throws BilleException, SQLException {
		Product p = new Product();
		
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM products WHERE product_id='" + id + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchProductIDException();
		}

		p.productID = rs.getString("product_id");
		p.category = rs.getString("category");
		p.name = rs.getString("name");
		p.code = rs.getString("code");
		p.quantity = rs.getFloat("quantity");
		p.unit = Unit.valueOf(rs.getString("unit"));
		p.pricePerUnit = rs.getFloat("price_per_unit");
		p.isAvailable = rs.getBoolean("is_available");
		
		return p;
	}

	// Getters

	public String getProductID() {
		return productID;
	}

	public String getCategory() {
		return category;
	}

	public String getName() {
		return name;
	}

	public String getCode() {
		return code;
	}

	public float getQuantity() {
		return quantity;
	}

	public Unit getUnit() {
		return unit;
	}

	public float getPricePerUnit() {
		return pricePerUnit;
	}

	public boolean isAvailable() {
		return isAvailable;
	}

	// Setters

	public void setProductID(String productID) {
		this.productID = productID;
	}

	public void setCategory(String catID) {
		this.category = catID;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setCode(String code) {
		this.code = code;
	}

	public void setQuantity(float quantity) {
		this.quantity = quantity;
	}

	public void setUnit(Unit unit) {
		this.unit = unit;
	}

	public void setPricePerUnit(float pricePerUnit) {
		this.pricePerUnit = pricePerUnit;
	}

	public void setAvailable(boolean isAvailable) {
		this.isAvailable = isAvailable;
	}

	// Validate

	private int validate() {
		if (productID == null || productID.equals("")) {
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
		if (quantity < 0) {
			return INVALID_QUANT;
		}
		if (unit == null) {
			return INVALID_UNIT;
		}
		if (pricePerUnit < 0) {
			return INVALID_PPU;
		}

		return 0;
	}

	// DB functions

	public int createNew() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (checkProductExistsByID(productID)) {
			throw new ProductIDExistsException();
		}
		if (checkProductExistsByCode(code)) {
			throw new ProductCodeExistsException();
		}
		if (!Category.checkCategoryExistsByID(category)) {
			throw new NoSuchCategoryIDException();
		}

		MainDB db = Application.getInstance().getMainDB();

		String sql = "INSERT INTO products VALUES (";
		sql += "'" + productID + "', ";
		sql += "'" + category + "', ";
		sql += "'" + name + "', ";
		sql += "'" + code + "', ";
		sql += quantity + ", ";
		sql += "'" + unit + "', ";
		sql += pricePerUnit + ", ";
		sql += (isAvailable ? "1" : "0");
		sql += ")";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public int update() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (!checkProductExistsByID(productID)) {
			throw new NoSuchProductIDException();
		}
		String prdID = getProductIDByCode(code);
		if (prdID != null && !prdID.equals(productID)) {
			throw new ProductCodeExistsException();
		}
		if (!Category.checkCategoryExistsByID(category)) {
			throw new NoSuchCategoryIDException();
		}

		MainDB db = Application.getInstance().getMainDB();

		String sql = "UPDATE products SET ";
		sql += "category='" + category + "', ";
		sql += "name='" + name + "', ";
		sql += "code='" + code + "', ";
		sql += "quantity=" + quantity + ", ";
		sql += "unit='" + unit + "', ";
		sql += "price_per_unit=" + pricePerUnit + ", ";
		sql += "is_available=" + (isAvailable ? "1" : "0");
		sql += " WHERE product_id='" + productID + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public static boolean checkProductExistsByCode(String code)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT product_id FROM products WHERE code='" + code
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static boolean checkProductExistsByID(String productID)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT product_id FROM products WHERE product_id='"
				+ productID + "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static String getProductIDByCode(String code) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT product_id FROM products WHERE code='" + code
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return null;
		}

		return rs.getString("product_id");
	}

	public static boolean delete(String productID) throws SQLException, BilleException {
		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}
		
		MainDB db = Application.getInstance().getMainDB();
		String sql = "DELETE FROM products WHERE product_id='" + productID
				+ "'";

		return db.executeSQL(sql);
	}

	// Search function

	public static ArrayList<Product> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		ArrayList<Product> productsList = new ArrayList<Product>();

		while (rs.next()) {
			String id = rs.getString("product_id");
			String category = rs.getString("category");
			String name = rs.getString("name");
			String code = rs.getString("code");
			float quantity = rs.getFloat("quantity");
			Unit unit = Unit.valueOf(rs.getString("unit"));
			float pricePerUnit = rs.getFloat("price_per_unit");
			boolean isAvailable = rs.getBoolean("is_available");
			
			Product p = new Product(id, name, category, code, quantity, unit, pricePerUnit, isAvailable);
			productsList.add(p);
		}

		return productsList;
	}

}
