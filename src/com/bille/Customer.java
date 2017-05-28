package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.CustomerIDExistsException;
import com.bille.exceptions.CustomerPhoneNumExistsException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchCustomerIDException;

public class Customer {

	public static final int INVALID_ID = -1, INVALID_FIRSTNAME = -2,
			INVALID_LASTNAME = -3, INVALID_PHONE_NUM = -4, INVALID_AGE = -5,
			INVALID_SEX = -6;

	private String customerID;
	private String firstName;
	private String lastName;

	private String phoneNum;
	private int age;
	private char sex;

	public Customer() {

	}

	public Customer(String id, String firstName, String lastName,
			String phoneNum, int age, char sex) {
		this.customerID = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.phoneNum = phoneNum;
		this.age = age;
		this.sex = sex;
	}

	// Construct from DB using phone number

	public Customer(String phoneNum) throws BilleException, SQLException {

		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM customers WHERE phone_num='" + phoneNum
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchCustomerIDException();
		}

		this.customerID = rs.getString("customer_id");
		this.firstName = rs.getString("first_name");
		this.lastName = rs.getString("last_name");
		this.phoneNum = rs.getString("phone_num");
		this.age = rs.getInt("age");
		this.sex = rs.getString("sex").charAt(0);
	}
	
	public static Customer getInstanceByID(String id) throws BilleException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM customers WHERE customer_id='" + id + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchCustomerIDException();
		}

		Customer c = new Customer();
		
		c.customerID = rs.getString("customer_id");
		c.firstName = rs.getString("first_name");
		c.lastName = rs.getString("last_name");
		c.phoneNum = rs.getString("phone_num");
		c.age = rs.getInt("age");
		c.sex = rs.getString("sex").charAt(0);
		
		return c;
	}

	// Get error messages

	public static String getErrMsg(int errCode) {
		return Integer.toString(errCode);
	}

	// Getters and setters

	public String getCustomerID() {
		return customerID;
	}

	public void setCustomerID(String customerID) {
		this.customerID = customerID;
	}

	public String getFirstName() {
		return firstName;
	}

	public void setFirstName(String firstName) {
		this.firstName = firstName;
	}

	public String getLastName() {
		return lastName;
	}

	public void setLastName(String lastName) {
		this.lastName = lastName;
	}

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public int getAge() {
		return age;
	}

	public void setAge(int age) {
		this.age = age;
	}

	public char getSex() {
		return sex;
	}

	public void setSex(char sex) {
		this.sex = sex;
	}

	// Validation

	private int validate() {
		if (customerID == null | customerID.equals("")) {
			return INVALID_ID;
		}
		if (firstName == null | firstName.equals("")) {
			return INVALID_FIRSTNAME;
		}
		if (lastName == null | lastName.equals("")) {
			return INVALID_LASTNAME;
		}
		if (age <= 0) {
			return INVALID_AGE;
		}
		if (sex != 'm' & sex != 'f') {
			return INVALID_SEX;
		}
		if (phoneNum == null | phoneNum.equals("")) {
			return INVALID_PHONE_NUM;
		}

		return 0;
	}

	// DB functions

	public int createNew() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}

		if (checkCustomerExistsByID(customerID)) {
			throw new CustomerIDExistsException();
		}
		if (checkCustoemrExistsByPhoneNum(phoneNum)) {
			throw new CustomerPhoneNumExistsException();
		}

		MainDB db = Application.getInstance().getMainDB();
		String sql = "INSERT INTO customers VALUES (";

		sql += "'" + customerID + "', ";
		sql += "'" + firstName + "', ";
		sql += "'" + lastName + "', ";
		sql += "'" + phoneNum + "', ";
		sql += age + ", ";
		sql += "'" + sex + "'";

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

		if (!checkCustomerExistsByID(customerID)) {
			throw new NoSuchCustomerIDException();
		}
		String custID = getCustomerIDByPhoneNum(phoneNum);
		if (custID != null && !custID.equals(customerID)) {
			throw new CustomerPhoneNumExistsException();
		}

		MainDB db = Application.getInstance().getMainDB();

		String sql = "UPDATE customers SET ";

		sql += "first_name='" + firstName + "', ";
		sql += "last_name='" + lastName + "', ";
		sql += "phone_num='" + phoneNum + "', ";
		sql += "age=" + age + ", ";
		sql += "sex='" + sex + "'";

		sql += " WHERE customer_id='" + customerID + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public static boolean checkCustomerExistsByID(String customerID)
			throws SQLException {

		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT customer_id FROM customers WHERE customer_id='"
				+ customerID + "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static boolean checkCustoemrExistsByPhoneNum(String phoneNum)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT customer_id FROM customers WHERE phone_num='"
				+ phoneNum + "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static String getCustomerIDByPhoneNum(String phoneNum)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT customer_id FROM customers WHERE phone_num='"
				+ phoneNum + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return null;
		}

		return rs.getString("customer_id");
	}

	public static boolean delete(String customerID) throws BilleException,
			SQLException {

		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}

		MainDB db = Application.getInstance().getMainDB();
		String sql = "DELETE FROM customers WHERE customer_id='" + customerID
				+ "'";

		return db.executeSQL(sql);
	}

	// Search function

	public static ArrayList<Customer> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		ArrayList<Customer> customerList = new ArrayList<Customer>();

		while (rs.next()) {
			String id = rs.getString("customer_id");
			String firstName = rs.getString("first_name");
			String lastName = rs.getString("last_name");
			String phoneNum = rs.getString("phone_num");
			int age = rs.getInt("age");
			char sex = rs.getString("sex").charAt(0);

			Customer c = new Customer(id, firstName, lastName, phoneNum, age,
					sex);
			customerList.add(c);
		}

		return customerList;
	}

}
