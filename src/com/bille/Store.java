package com.bille;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.bille.exceptions.*;

public class Store {
	
	public static final int INVALID_STORE_TYPE = -1, INVALID_ADDRESS = -2,
			INVALID_PHONE_ONE = -3;
	
	private LiteDB liteDB = Application.getInstance().getLiteDB();
	
	private String storeID;
	
	private String dbName, dbUser, dbPassword;
	
	private String name;
	private StoreType type;
	private String address;
	private String phoneOne;
	private String phoneTwo;
	private String phoneThree;
	private String email;
	private String website;
	private Date expires;
	
	// Construct from DB
	
	public Store() throws BilleException, SQLException {
		readData();
	}
	
	public void readData() throws BilleException, SQLException {
		String sql = "SELECT * FROM store WHERE id='1'";
		ResultSet rs = liteDB.fetchRows(sql);
		
		if (!rs.next()) {
			throw new LocalDBException();
		}
		
		String key = new String(this.key);
		
		this.storeID = Crypt.decrypt(rs.getString("store_id"), key);
		
		this.dbName = Crypt.decrypt(rs.getString("db_name"), key);
		this.dbUser = Crypt.decrypt(rs.getString("db_user"), key);
		this.dbPassword = Crypt.decrypt(rs.getString("db_password"), key);
		
		String dateStr = Crypt.decrypt(rs.getString("expires"), key);
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		try {
			expires = df.parse(dateStr);
		} catch (ParseException e) {
			return;
		}
						
		this.name = Crypt.decrypt(rs.getString("name"), key);
		this.type = StoreType.valueOf(Crypt.decrypt(rs.getString("type"), key));
		this.address = Crypt.decrypt(rs.getString("address"), key);
		this.phoneOne = Crypt.decrypt(rs.getString("phone_one"), key);
		this.phoneTwo = Crypt.decrypt(rs.getString("phone_two"), key);
		this.phoneThree = Crypt.decrypt(rs.getString("phone_three"), key);
		this.email = Crypt.decrypt(rs.getString("email"), key);
		this.website = Crypt.decrypt(rs.getString("website"), key);		
	}

	// Getters and setters
	
	public String getStoreID() {
		return storeID;
	}
	
	public Date getExpires() {
		return expires;
	}
	
	public String getDBName() {
		return dbName;
	}
	
	public void setDBName(String dbName) {
		this.dbName = dbName;
	}
	
	public String getDBUser() {
		return dbUser;
	}
	
	public void setDBUser(String dbUser) {
		this.dbUser = dbUser;
	}
	
	public String getDBPassword() {
		return dbPassword;
	}
	
	public void setDBPassword(String password) {
		this.dbPassword = password;
	}
	
	public StoreType getType() {
		return type;
	}

	public void setType(StoreType type) {
		this.type = type;
	}
	
	public String getName() {
		return this.name;
	}
	
	public void setName(String name) {
		this.name = name;
	}

	public String getAddress() {
		return address;
	}

	public void setAddress(String address) {
		this.address = address;
	}

	public String getPhoneOne() {
		return phoneOne;
	}

	public void setPhoneOne(String phoneOne) {
		this.phoneOne = phoneOne;
	}

	public String getPhoneTwo() {
		return phoneTwo;
	}

	public void setPhoneTwo(String phoneTwo) {
		this.phoneTwo = phoneTwo;
	}

	public String getPhoneThree() {
		return phoneThree;
	}

	public void setPhoneThree(String phoneThree) {
		this.phoneThree = phoneThree;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public String getWebsite() {
		return website;
	}

	public void setWebsite(String website) {
		this.website = website;
	}
	
	// Validation
	
	private int validate() {
		if (type == null) {
			return INVALID_STORE_TYPE;
		}
		if (address == null | address == "") {
			return INVALID_ADDRESS;
		}
		if (phoneOne == null | phoneOne == "") {
			return INVALID_PHONE_ONE;
		}
		if (phoneTwo == null) {
			phoneTwo = "";
		}
		if (phoneThree == null) {
			phoneThree = "";
		}
		if (email == null) {
			email = "";
		}
		if (website == null) {
			website = "";
		}
		
		return 0;
	}
	
	private void sanitize() {
		name = name.replace("'", "`");
		address = address.replace("'", "`");
	}
	
	// DB functions
	
	public int updateLicense(License lic) throws BilleException, SQLException {
		if (type == StoreType.RESTO & RestoOrder.isAnyOrderActive()) {
			throw new ActiveOrderPresentException();
		}
		
		SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
		String dateStr = df.format(lic.getExpires());
		
		String key = new String(this.key);
		
		String sql = "UPDATE store SET ";
		sql += "name='" + Crypt.encrypt(lic.getName(), key) + "', ";
		sql += "address='" + Crypt.encrypt(lic.getAddress(), key) + "', ";
		sql += "phone_one='" + Crypt.encrypt(lic.getPhoneOne(), key) + "', "; 
		sql += "phone_two='" + Crypt.encrypt(lic.getPhoneTwo(), key) + "', "; 
		sql += "phone_three='" + Crypt.encrypt(lic.getPhoneThree(), key) + "', "; 
		sql += "email='" + Crypt.encrypt(lic.getEmail(), key) + "', "; 
		sql += "website='" + Crypt.encrypt(lic.getWebsite(), key) + "', "; 
		sql += "type='" + Crypt.encrypt(lic.getType().toString(), key) + "', "; 
		sql += "expires='" + Crypt.encrypt(dateStr, key) + "'"; 

		sql += " WHERE store_id='" + Crypt.encrypt(storeID, new String(key)) + "'";
		
		if (liteDB.executeSQL(sql)) {
			return 0;
		}
		
		throw new LocalDBException();
	}
	
	public int update() throws BilleException, SQLException {
		
		int ret = validate();
		if (ret != 0) {
			return ret;
		}
		
		if (type == StoreType.RESTO & RestoOrder.isAnyOrderActive()) {
			throw new ActiveOrderPresentException();
		}
		
		String key = new String(this.key);
				
		String sql = "UPDATE store SET ";
		sql += "db_name='" + Crypt.encrypt(dbName, key) + "', ";
		sql += "db_user='" + Crypt.encrypt(dbUser, key) + "', ";
		sql += "db_password='" + Crypt.encrypt(dbPassword, key) + "'"; 
		sql += " WHERE store_id='" + Crypt.encrypt(storeID, key) + "'";
		
		if (liteDB.executeSQL(sql)) {
			return 0;
		}
		
		throw new LocalDBException();
	}
	
	private byte[] key = {'1','b','f','d','4','7','2','c','c','f','c','0','4','3','5','c'};

}
