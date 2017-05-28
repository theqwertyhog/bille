package com.bille;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;

import com.bille.exceptions.ActiveOrderPresentException;
import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.NoSuchStaffIDException;
import com.bille.exceptions.PasswordsDontMatchException;
import com.bille.exceptions.StaffIDExistsException;
import com.bille.exceptions.StaffUserIDExistsException;

public class Staff {

	public static final int INVALID_ID = -1, INVALID_FIRSTNAME = -2,
			INVALID_LASTNAME = -3, INVALID_AGE = -4, INVALID_SEX = -4,
			INVALID_PHONE_NUM = -5, INVALID_ADDRESS = -6, INVALID_ROLE = -7,
			INVALID_USERID = -8, INVALID_PASSWORD = -9;

	private String staffID;

	private String firstName;
	private String lastName;
	private int age;
	private char sex;
	private String phoneNum;
	private String address;
	private UserRole role;

	private String userID;
	private boolean isActive;

	private String password;
	
	public Staff() {
		
	}

	public Staff(String id, String firstName, String lastName, int age,
			char sex, String phoneNum, String address, UserRole role,
			String userID, boolean isActive) {
		this.staffID = id;
		this.firstName = firstName;
		this.lastName = lastName;
		this.age = age;
		this.sex = sex;
		this.phoneNum = phoneNum;
		this.address = address;
		this.role = role;
		this.userID = userID;
		this.isActive = isActive;
	}

	// Construct from DB using user ID

	public Staff(String userID) throws NoSuchStaffIDException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM staff WHERE user_id='" + userID + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchStaffIDException();
		}

		this.staffID = rs.getString("staff_id");
		this.firstName = rs.getString("first_name");
		this.lastName = rs.getString("last_name");
		this.age = rs.getInt("age");
		this.sex = rs.getString("sex").charAt(0);
		this.phoneNum = rs.getString("phone_num");
		this.address = rs.getString("address");
		
		this.role = UserRole.valueOf(rs.getString("role"));
		
		this.userID = rs.getString("user_id");
		this.isActive = rs.getBoolean("is_active");
	}
	
	public static Staff getInstanceByID(String id) throws NoSuchStaffIDException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT * FROM staff WHERE staff_id='" + id + "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchStaffIDException();
		}
		
		Staff s = new Staff();

		s.staffID = rs.getString("staff_id");
		s.firstName = rs.getString("first_name");
		s.lastName = rs.getString("last_name");
		s.age = rs.getInt("age");
		s.sex = rs.getString("sex").charAt(0);
		s.phoneNum = rs.getString("phone_num");
		s.address = rs.getString("address");
		
		s.role = UserRole.valueOf(rs.getString("role"));
		
		s.userID = rs.getString("user_id");
		s.isActive = rs.getBoolean("is_active");
		
		return s;
	}
	
	// Get error messages
	
	public static String getErrMsg(int errCode) {
		return Integer.toString(errCode);
	}

	// Getters and setters

	public String getStaffID() {
		return staffID;
	}

	public void setStaffID(String staffID) {
		this.staffID = staffID;
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

	public String getPhoneNum() {
		return phoneNum;
	}

	public void setPhoneNum(String phoneNum) {
		this.phoneNum = phoneNum;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public UserRole getRole() {
		return role;
	}

	public void setRole(UserRole role) {
		this.role = role;
	}

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public boolean isActive() {
		return isActive;
	}

	public void setActive(boolean isActive) {
		this.isActive = isActive;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	// Validation

	private int validate() {
		if (staffID == null | staffID.equals("")) {
			return INVALID_ID;
		}
		if (firstName == null | firstName.equals("")) {
			return INVALID_FIRSTNAME;
		}
		if (lastName == null | lastName.equals("")) {
			return INVALID_LASTNAME;
		}
		if (age < 18) {
			return INVALID_AGE;
		}
		if (sex != 'm' & sex != 'f') {
			return INVALID_SEX;
		}
		if (phoneNum == null | phoneNum.equals("")) {
			return INVALID_PHONE_NUM;
		}
		if (address == null | address.equals("Address")) {
			address = "";
		}
		if (role == null) {
			return INVALID_ROLE;
		}
		if (userID == null | userID.equals("")) {
			return INVALID_USERID;
		}

		return 0;
	}

	// Password functions

	public static String readPassword(String userID) throws NoSuchStaffIDException, SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT password FROM staff WHERE user_id='" + userID
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			throw new NoSuchStaffIDException();
		}

		String passStr = Crypt.decrypt(rs.getString("password"), new String(key));

		return passStr;
	}

	public static int updatePassword(String userID, String oldPassword,
			String newPassword) throws BilleException, SQLException {

		if (newPassword.equals("") | newPassword == null) {
			return INVALID_PASSWORD;
		}

		if (!checkStaffExistsByUserID(userID)) {
			throw new NoSuchStaffIDException();
		}

		String originalPassword = readPassword(userID);

		if (!oldPassword.equals(originalPassword)) {
			throw new PasswordsDontMatchException();
		}

		MainDB db = Application.getInstance().getMainDB();
		String sql = "UPDATE staff SET password='" + Crypt.encrypt(newPassword, new String(key))
				+ "'";
		sql += " WHERE user_id='" + userID + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	// DB functions

	public int createNew() throws BilleException, SQLException {

		int ret = validate();
		if (ret != 0) {
			return ret;
		}
		
		if (password.equals("") | password == null) {
			return INVALID_PASSWORD;
		}

		if (checkStaffExistsbyID(staffID)) {
			throw new StaffIDExistsException();
		}
		if (checkStaffExistsByUserID(userID)) {
			throw new StaffUserIDExistsException();
		}

		MainDB db = Application.getInstance().getMainDB();

		String sql = "INSERT INTO staff VALUES(";
		sql += "'" + staffID + "', ";
		sql += "'" + firstName + "', ";
		sql += "'" + lastName + "', ";
		sql += age + ", ";
		sql += "'" + sex + "', ";
		sql += "'" + phoneNum + "', ";
		sql += "'" + address + "', ";
		sql += "'" + role + "', ";
		sql += "'" + userID + "', ";
		sql += "'" + password + "', ";
		sql += (isActive ? "1" : "0");
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

		if (!checkStaffExistsbyID(staffID)) {
			throw new NoSuchStaffIDException();
		}
		String stfID = getStaffIDByUserID(userID);
		if (stfID != null && !stfID.equals(staffID)) {
			throw new StaffUserIDExistsException();
		}

		MainDB db = Application.getInstance().getMainDB();

		String sql = "UPDATE staff SET ";
		sql += "first_name='" + firstName + "', ";
		sql += "last_name='" + lastName + "', ";
		sql += "age=" + age + ", ";
		sql += "sex='" + sex + "', ";
		sql += "phone_num='" + phoneNum + "', ";
		sql += "address='" + address + "', ";
		sql += "role='" + role + "', ";
		sql += "user_id='" + userID + "', ";
		sql += "is_active=" + (isActive ? "1" : "0");
		sql += " WHERE staff_id='" + staffID + "'";

		if (db.executeSQL(sql)) {
			return 0;
		}

		throw new DatabaseException();
	}

	public static boolean checkStaffExistsbyID(String staffID)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT staff_id FROM staff WHERE staff_id='" + staffID
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static boolean checkStaffExistsByUserID(String userID)
			throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT staff_id FROM staff WHERE user_id='" + userID
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (rs.next()) {
			return true;
		}

		return false;
	}

	public static String getStaffIDByUserID(String userID) throws SQLException {
		MainDB db = Application.getInstance().getMainDB();
		String sql = "SELECT staff_id FROM staff WHERE user_id='" + userID
				+ "'";

		ResultSet rs = db.fetchRows(sql);

		if (!rs.next()) {
			return null;
		}

		return rs.getString("staff_id");
	}

	public static boolean delete(String staffID) throws BilleException, SQLException {

		if (Application.getInstance().getStore().getType() == StoreType.RESTO) {
			if (RestoOrder.isAnyOrderActive()) {
				throw new ActiveOrderPresentException();
			}
		}

		MainDB db = Application.getInstance().getMainDB();
		String sql = "DELETE FROM staff WHERE staff_id='" + staffID + "'";

		return db.executeSQL(sql);
	}

	// Search function

	public static ArrayList<Staff> search(String sql) throws SQLException {
		ResultSet rs = Application.getInstance().getMainDB().fetchRows(sql);

		ArrayList<Staff> staffList = new ArrayList<Staff>();

		while (rs.next()) {
			String id = rs.getString("staff_id");
			String firstName = rs.getString("first_name");
			String lastName = rs.getString("last_name");
			int age = rs.getInt("age");
			char sex = rs.getString("sex").charAt(0);
			String phoneNum = rs.getString("phone_num");
			String address = rs.getString("address");
			UserRole role = UserRole.valueOf(rs.getString("role"));
			String userID = rs.getString("user_id");
			boolean isActive = rs.getBoolean("is_active");

			Staff s = new Staff(id, firstName, lastName, age, sex, phoneNum,
					address, role, userID, isActive);
			staffList.add(s);
		}

		return staffList;
	}

	private static final byte[] key = {'b', '3', '4' , '7', '5', '2', '0', 'a', 'b',
			'2', 'e', 'f', 'f', '8', '5', '3'};
}
