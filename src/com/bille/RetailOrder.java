package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.ArrayList;
import java.util.HashMap;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchCustomerIDException;
import com.bille.exceptions.NoSuchOrderIDException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.exceptions.OrderIDExistsException;
import com.trolltech.qt.gui.QMessageBox;

public class RetailOrder {

	public static final int INVALID_ID = -1, INVALID_CUSTOMER_ID = -2,
			INVALID_TAKEN_AT = -3, INVALID_TAKEN_BY = -4,
			INVALID_CLOSED_AT = -5, INVALID_CLOSED_BY = -6,
			INVALID_BILL_AMT = -7, INVALID_AMT_PAID = -8,
			INVALID_PAYMENT_TYPE = -9;

	private MainDB db = Application.getInstance().getMainDB();

	private String orderID;
	private String customerID;

	private Date takenAt;
	private String takenBy;

	private float billAmount;
	private float amountPaid;
	private String discountReason;
	private PaymentType paymentType;

	private String comments;

	private HashMap<String, Float> products = new HashMap<String, Float>();

	public RetailOrder() {

	}

	public RetailOrder(String orderID, String customerID) {
		this.orderID = orderID;
		this.customerID = customerID;
	}

	// Construct from DB using orderID

	public RetailOrder(String orderID) throws BilleException, SQLException {
		String sql = "SELECT * FROM retail_orders WHERE order_id='" + orderID
				+ "'";
		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchOrderIDException();
		}

		this.orderID = orderID;
		this.customerID = rs.getString(customerID);
		this.billAmount = rs.getFloat("bill_amount");
		this.amountPaid = rs.getFloat("amount_paid");
		this.discountReason = rs.getString("discount_reason");
		this.paymentType = PaymentType.valueOf(rs.getString("payment_type"));
		this.comments = rs.getString("comments");
		this.takenAt = rs.getTimestamp("taken_at");
		this.takenBy = rs.getString("taken_by");
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

	// Product functions

	public void addProduct(String productID, float qty) {
		float quant = 0;

		if (products.containsKey(productID)) {
			quant = products.get(productID);
			quant += qty;
		} else {
			quant = qty;
		}

		products.put(productID, quant);
	}

	public void removeProduct(String productID) {
		products.remove(productID);
	}

	public HashMap<String, Float> getProducts() {
		return products;
	}

	// Validation

	private int validate() {
		if (orderID == null || orderID.equals("")) {
			return INVALID_ID;
		}
		if (customerID == null || customerID.equals("")) {
			return INVALID_CUSTOMER_ID;
		}
		if (takenBy == null || takenBy.equals("")) {
			return INVALID_TAKEN_BY;
		}

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

		return 0;
	}

	// DB functions

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

		String sql = "INSERT INTO retail_orders VALUES (";
		sql += "'" + orderID + "'";
		sql += "'" + customerID + "', ";
		sql += billAmount + ", ";
		sql += amountPaid + ", ";
		sql += "'" + discountReason + "', ";
		sql += "'" + paymentType + "', ";
		sql += "'" + comments + "', ";
		sql += "FROM_UNIXTIME(" + (System.currentTimeMillis() / 1000) + "), ";
		sql += "'" + takenBy + "'";
		sql += ");";

		if (!db.executeSQL(sql)) {
			throw new DatabaseException();
		}

		boolean isProductsRemoved = false;
		for (String productID : products.keySet()) {
			if (!Product.checkProductExistsByID(productID)) {
				products.remove(productID);
				isProductsRemoved = true;
				continue;
			}

			Product prd = new Product(productID);
			float qty = products.get(productID);

			sql = "INSERT INTO order_products VALUES (";
			sql += "'" + productID + "', ";
			sql += "'" + orderID + "', ";
			sql += "'" + prd.getName() + "', ";
			sql += qty + ", ";
			sql += prd.getPricePerUnit();
			sql += "'" + prd.getUnit() + "'";
			sql += ");";

			if (!db.executeSQL(sql)) {
				throw new DatabaseException();
			}
		}

		Tax[] taxes = Application.getInstance().getApplicableTaxes();

		for (Tax t : taxes) {
			sql = "INSERT INTO order_taxes VALUES (";
			sql += "'" + orderID + "', ";
			sql += "'" + t.getTaxID() + "', ";
			sql += "'" + t.getName() + "', ";
			sql += t.getValue();
			sql += ");";

			if (!db.executeSQL(sql)) {
				throw new DatabaseException();
			}
		}

		if (isProductsRemoved) {
			QMessageBox
					.warning(Application.getMainWidget(), "Products Removed",
							"Some products might be unavailable and have been removed.");
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

		String sql = "UPDATE retail_orders SET ";
		sql += "customer_id='" + customerID + "', ";
		sql += "bill_amount=" + billAmount + ", ";
		sql += "amount_paid=" + amountPaid + ", ";
		sql += "discount_reason='" + discountReason + "', ";
		sql += "payment_type='" + paymentType + "', ";
		sql += "comments='" + comments + "', ";
		sql += "taken_at=" + takenAt.getTime() + ", ";
		sql += "taken_by='" + takenBy + "'";
		sql += " WHERE order_id='" + orderID + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public int updateProducts() throws BilleException, SQLException {
		if (!checkOrderExists(orderID)) {
			throw new NoSuchOrderIDException();
		}

		String sql = "DELETE FROM order_products WHERE order_id='" + orderID
				+ "';";

		boolean isProductsRemoved = false;
		for (String productID : products.keySet()) {
			if (!Product.checkProductExistsByID(productID)) {
				products.remove(productID);
				isProductsRemoved = true;
				continue;
			}

			Product prd = new Product(productID);
			float qty = products.get(productID);

			sql += "INSERT INTO order_products VALUES (";
			sql += "'" + productID + "', ";
			sql += "'" + orderID + "', ";
			sql += "'" + prd.getName() + "', ";
			sql += qty + ", ";
			sql += prd.getPricePerUnit();
			sql += "'" + prd.getUnit() + "'";
			sql += ");";
		}

		if (db.executeSQL(sql)) {
			if (isProductsRemoved) {
				QMessageBox
						.warning(Application.getMainWidget(),
								"Products Removed",
								"Some products might be unavailable and have been removed.");
			}
			return 0;
		}

		throw new DatabaseException();
	}

	public int updateTaxes() throws BilleException, SQLException {
		if (!checkOrderExists(orderID)) {
			throw new NoSuchOrderIDException();
		}

		Tax[] taxes = Application.getInstance().getApplicableTaxes();

		String sql = "";

		for (Tax t : taxes) {
			sql += "INSERT OR UPDATE INTO order_taxes VALUES (";
			sql += "'" + orderID + "', ";
			sql += "'" + t.getTaxID() + "', ";
			sql += "'" + t.getName() + "', ";
			sql += t.getValue();
			sql += ") ";
			sql += "WHERE order_id='" + orderID + "';";
		}

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public static boolean checkOrderExists(String orderID) throws SQLException {
		String sql = "SELECT order_id FROM retail_orders WHERE order_id='"
				+ orderID + "'";
		MainDB db = Application.getInstance().getMainDB();

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return false;
		}

		return true;
	}

	public static boolean delete(String orderID) throws BilleException,
			SQLException {
		if (RestoOrder.isAnyOrderActive()) {
			throw new ActiveOrderPresentException();
		}

		String sql = "DELETE FROM retail_orders WHERE order_id='" + orderID
				+ "';";
		sql += "DELETE FROM order_products WHERE order_id='" + orderID + "';";
		sql += "DELETE FROM order_taxes WHERE order_id='" + orderID + "';";

		return Application.getInstance().getMainDB().executeSQL(sql);
	}

}