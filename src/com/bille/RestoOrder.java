package com.bille;

import java.util.Date;
import java.util.HashMap;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchCustomerIDException;
import com.bille.exceptions.NoSuchOrderIDException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.exceptions.OrderIDExistsException;
import com.trolltech.qt.gui.QMessageBox;

public class RestoOrder {

	public static final int INVALID_ID = -1, INVALID_CUSTOMER_ID = -2,
			INVALID_TABLE_NO = -3, INVALID_ORDER_TYPE = -4,
			INVALID_TAKEN_AT = -5, INVALID_TAKEN_BY = -6,
			INVALID_CLOSED_AT = -7, INVALID_CLOSED_BY = -8,
			INVALID_BILL_AMT = -9, INVALID_AMT_PAID = -10,
			INVALID_PAYMENT_TYPE = -11;

	private MainDB db = Application.getInstance().getMainDB();

	private String orderID;
	private String customerID;

	private int tableNo;
	private RestoOrderType orderType;
	private boolean isActive = true;

	private Date takenAt;
	private String takenBy;
	private Date closedAt;
	private String closedBy;

	private float billAmount;
	private float amountPaid;
	private String discountReason;
	private PaymentType paymentType;

	private String comments;

	private HashMap<String, Integer> items = new HashMap<String, Integer>();
	private ArrayList<Tax> taxes = new ArrayList<Tax>();

	public RestoOrder() {

	}

	public RestoOrder(String id, String customerID, int tableNo,
			RestoOrderType orderType) {
		this.orderID = id;
		this.customerID = customerID;
		this.tableNo = tableNo;
		this.orderType = orderType;
	}

	// Construct from DB using orderID, doesn't load items or taxes

	public RestoOrder(String orderID) throws BilleException, SQLException {

		String sql = "SELECT * FROM resto_orders WHERE order_id='" + orderID
				+ "'";
		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchOrderIDException();
		}

		this.orderID = orderID;
		this.customerID = rs.getString("customer_id");
		this.tableNo = rs.getInt("table_no");
		this.orderType = RestoOrderType.valueOf(rs.getString("type"));
		this.billAmount = rs.getFloat("bill_amount");
		this.amountPaid = rs.getFloat("amount_paid");
		this.discountReason = rs.getString("discount_reason");

		try {
			this.paymentType = PaymentType
					.valueOf(rs.getString("payment_type"));
			this.takenAt = rs.getTimestamp("taken_at");
			this.closedAt = rs.getTimestamp("closed_at");
		} catch (NullPointerException e) {

		}

		this.comments = rs.getString("comments");
		this.takenBy = rs.getString("taken_by");
		this.closedBy = rs.getString("closed_by");
		this.isActive = rs.getBoolean("is_active");

		readItems();
		readTaxes();
	}

	public String getErrorMessage(int code) {
		return Integer.toString(code);
	}

	// Getters and setters

	public String getOrderID() {
		return orderID;
	}

	public void setOrderID(String orderID) {
		this.orderID = orderID;
	}

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public int getTableNo() {
		return tableNo;
	}

	public void setTableNo(int tableNo) {
		this.tableNo = tableNo;
	}

	public RestoOrderType getOrderType() {
		return orderType;
	}

	public void setOrderType(RestoOrderType orderType) {
		this.orderType = orderType;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public Date getTakenAt() {
		return takenAt;
	}

	public void setTakenAt(Date takenAt) {
		this.takenAt = takenAt;
	}

	public String getTakenBy() {
		return takenBy;
	}

	public void setTakenBy(String takenBy) {
		this.takenBy = takenBy;
	}

	public Date getClosedAt() {
		return closedAt;
	}

	public void setClosedAt(Date closedAt) {
		this.closedAt = closedAt;
	}

	public String getClosedBy() {
		return closedBy;
	}

	public void setClosedBy(String closedBy) {
		this.closedBy = closedBy;
	}

	public float getBillAmount() {
		return billAmount;
	}

	public void setBillAmount(float billAmount) {
		this.billAmount = billAmount;
	}

	public float getAmountPaid() {
		return amountPaid;
	}

	public void setAmountPaid(float amountPaid) {
		this.amountPaid = amountPaid;
	}

	public String getDiscountReason() {
		return discountReason;
	}

	public void setDiscountReason(String discountReason) {
		this.discountReason = discountReason;
	}

	public PaymentType getPaymentType() {
		return paymentType;
	}

	public void setPaymentType(PaymentType paymentType) {
		this.paymentType = paymentType;
	}

	public String getComments() {
		return comments;
	}

	public void setComments(String comments) {
		this.comments = comments;
	}

	// Item functions

	public void addItem(String itemID, int qty) {
		int quant = 0;

		if (items.containsKey(itemID)) {
			quant = items.get(itemID);
			quant += qty;
		} else {
			quant = qty;
		}

		items.put(itemID, quant);
	}

	public void removeItem(String itemID) {
		items.remove(itemID);
	}

	public void setItemQty(String itemID, int qty) {
		if (items.containsKey(itemID)) {
			items.put(itemID, qty);
		}
	}

	public HashMap<String, Integer> getItems() {
		return items;
	}

	// Validation

	private int validate() {
		if (orderID == null || orderID.equals("")) {
			return INVALID_ID;
		}
		if (customerID == null || customerID.equals("")) {
			return INVALID_CUSTOMER_ID;
		}
		if (tableNo <= 0) {
			return INVALID_TABLE_NO;
		}
		if (orderType == null) {
			return INVALID_ORDER_TYPE;
		}
		if (takenBy == null || takenBy.equals("")) {
			return INVALID_TAKEN_BY;
		}

		// Only check these if attempting to close order
		if (!isActive) {
			if (billAmount < 0) {
				return INVALID_BILL_AMT;
			}
			if (amountPaid < 0) {
				return INVALID_AMT_PAID;
			}
			if (discountReason == null) {
				discountReason = "";
			}
			if (paymentType == null) {
				return INVALID_PAYMENT_TYPE;
			}
			if (comments == null) {
				comments = "";
			}
			if (closedBy == "" | closedBy == null) {
				return INVALID_CLOSED_BY;
			}
		}

		return 0;
	}

	// DB functions

	private void readItems() throws SQLException {
		items.clear();

		String sql = "SELECT * FROM order_items WHERE order_id='" + orderID
				+ "'";
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		while (rs.next()) {
			String itemID = rs.getString("item_id");
			int quant = rs.getInt("quantity");

			items.put(itemID, quant);
		}
	}

	private void readTaxes() throws SQLException {
		taxes.clear();

		String sql = "SELECT * FROM order_taxes WHERE order_id='" + orderID
				+ "'";
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		while (rs.next()) {
			String taxID = rs.getString("tax_id");
			String name = rs.getString("name");
			float value = rs.getFloat("value");

			Tax t = new Tax(taxID, name, value, false);
			taxes.add(t);
		}
	}

	public int createNew() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (checkOrderExists(orderID)) {
			throw new OrderIDExistsException();
		}
		if (!Customer.checkCustomerExistsByID(customerID)) {
			throw new NoSuchCustomerIDException();
		}
		if (!Staff.checkStaffExistsbyID(takenBy)) {
			throw new NoSuchStaffIDException();
		}

		// Check only if attempting to close order
		if (!isActive) {
			if (!Staff.checkStaffExistsbyID(closedBy)) {
				throw new NoSuchStaffIDException();
			}
		}

		String sql = "INSERT INTO resto_orders ";

		sql += "(order_id, customer_id, table_no, type, comments, taken_at, taken_by";
		if (!isActive) {
			sql += ", bill_amount, amount_paid, discount_reason, payment_type, closed_at, closed_by";
		}
		sql += ", is_active) ";

		sql += "VALUES (";
		sql += "'" + orderID + "', ";
		sql += "'" + customerID + "', ";
		sql += tableNo + ", ";
		sql += "'" + orderType + "', ";
		sql += "'" + comments + "', ";
		sql += "FROM_UNIXTIME(" + (System.currentTimeMillis() / 1000) + "), ";
		sql += "'" + takenBy + "', ";

		if (!isActive) {
			sql += billAmount + ", ";
			sql += amountPaid + ", ";
			sql += "'" + discountReason + "', ";
			sql += "'" + paymentType + "', ";
			sql += "FROM_UNIXTIME(" + (System.currentTimeMillis() / 1000) + "), ";
			sql += "'" + closedBy + "', ";
		}

		sql += (isActive ? "1" : "0");
		sql += ");";

		if (!db.executeSQL(sql)) {
			throw new DatabaseException();
		}

		boolean isItemsRemoved = false;
		for (String itemID : items.keySet()) {
			if (!Item.checkItemExistsByID(itemID)) {
				items.remove(itemID);
				isItemsRemoved = true;
				continue;
			}

			Item itm = Item.getInstanceByID(itemID);
			float qty = items.get(itemID);

			sql = "INSERT INTO order_items VALUES (";
			sql += "'" + itemID + "', ";
			sql += "'" + orderID + "', ";
			sql += "'" + itm.getName() + "', ";
			sql += qty + ", ";
			sql += itm.getPrice();
			sql += ");";

			if (!db.executeSQL(sql)) {
				throw new DatabaseException();
			}
		}

		if (isItemsRemoved) {
			QMessageBox.warning(Application.getMainWidget(), "Items Removed",
					"Some items might be unavailable and have been removed.");
		}
		return 0;
	}

	// NOTE: Only updates the order, not the order items or taxes

	public int update() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (!checkOrderExists(orderID)) {
			throw new NoSuchOrderIDException();
		}

		if (!Customer.checkCustomerExistsByID(customerID)) {
			throw new NoSuchCustomerIDException();
		}
		if (!Staff.checkStaffExistsbyID(takenBy)) {
			throw new NoSuchStaffIDException();
		}

		// Check only if attempting to close order
		if (!isActive) {
			if (!Staff.checkStaffExistsbyID(closedBy)) {
				throw new NoSuchStaffIDException();
			}
		}

		String sql = "UPDATE resto_orders SET ";
		sql += "customer_id='" + customerID + "', ";
		sql += "table_no=" + tableNo + ", ";
		sql += "type='" + orderType + "', ";

		if (!isActive) {
			sql += "bill_amount=" + billAmount + ", ";
			sql += "amount_paid=" + amountPaid + ", ";
			sql += "discount_reason='" + discountReason + "', ";
			sql += "payment_type='" + paymentType + "', ";
			sql += "closed_at=" + (System.currentTimeMillis() / 1000) + ", ";
			sql += "closed_by='" + closedBy + "', ";
		}

		sql += "comments='" + comments + "', ";

		sql += "is_active=" + (isActive ? "1" : "0");
		sql += " WHERE order_id='" + orderID + "'";
		
		if (!db.executeSQL(sql)) {
			throw new DatabaseException();
		}

		return updateItems();
	}

	public int updateItems() throws BilleException, SQLException {
		if (!checkOrderExists(orderID)) {
			throw new NoSuchOrderIDException();
		}

		String sql = "DELETE FROM order_items WHERE order_id='" + orderID
				+ "';";

		if (!db.executeSQL(sql)) {
			throw new DatabaseException();
		}

		boolean isItemsRemoved = false;
		for (String itemID : items.keySet()) {
			if (!Item.checkItemExistsByID(itemID)) {
				items.remove(itemID);
				isItemsRemoved = true;
				continue;
			}

			Item itm = Item.getInstanceByID(itemID);
			float qty = items.get(itemID);

			sql = "INSERT INTO order_items VALUES(";
			sql += "'" + itemID + "', ";
			sql += "'" + orderID + "', ";
			sql += "'" + itm.getName() + "', ";
			sql += qty + ", ";
			sql += itm.getPrice();
			sql += ");";

			if (!db.executeSQL(sql)) {
				throw new DatabaseException();
			}
		}

		if (isItemsRemoved) {
			QMessageBox.warning(Application.getMainWidget(), "Items Removed",
					"Some items might be unavailable and have been removed.");
		}
		return 0;
	}

	public int updateTaxes() throws BilleException, SQLException {
		if (!checkOrderExists(orderID)) {
			throw new NoSuchOrderIDException();
		}

		Tax[] taxes = Application.getInstance().getApplicableTaxes();

		String sql = "";

		for (Tax t : taxes) {
			sql = "INSERT INTO order_taxes VALUES (";
			sql += "'" + orderID + "', ";
			sql += "'" + t.getTaxID() + "', ";
			sql += "'" + t.getName() + "', ";
			sql += t.getValue();
			sql += ") ";
			sql += "WHERE order_id='" + orderID + "';";

			if (!db.executeSQL(sql)) {
				throw new DatabaseException();
			}
		}

		return 0;
	}

	public static boolean checkOrderExists(String id) throws SQLException {
		String sql = "SELECT order_id FROM resto_orders WHERE order_id='" + id
				+ "'";
		MainDB db = Application.getInstance().getMainDB();

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return false;
		}

		return true;
	}

	public static boolean delete(String orderID) throws BilleException,
			SQLException {
		
		MainDB db = Application.getInstance().getMainDB();

		String sql = "DELETE FROM resto_orders WHERE order_id='" + orderID
				+ "';";
		boolean ret = db.executeSQL(sql);
		
		sql = "DELETE FROM order_items WHERE order_id='" + orderID + "';";
		ret = db.executeSQL(sql);
		
		sql = "DELETE FROM order_taxes WHERE order_id='" + orderID + "';";
		ret = db.executeSQL(sql);

		return ret;
	}

	public static String[] getActiveOrders() throws SQLException {
		String sql = "SELECT order_id FROM resto_orders WHERE is_active=1";
		MainDB db = Application.getInstance().getMainDB();

		ResultSet rs = db.fetchRows(sql);

		ArrayList<String> orders = new ArrayList<String>();

		while (rs.next()) {
			orders.add(rs.getString("order_id"));
		}
		
		if (orders.size() == 0) {
			return null;
		}

		String[] ordersArray = new String[orders.size() - 1];
		return orders.toArray(ordersArray);
	}

	public static boolean isAnyOrderActive() throws SQLException {
		String sql = "SELECT order_id FROM resto_orders WHERE is_active=1";
		MainDB db = Application.getInstance().getMainDB();

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

}
