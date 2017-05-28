package com.bille;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Date;
import java.util.HashMap;

import com.bille.exceptions.BilleException;
import com.bille.exceptions.DatabaseException;
import com.bille.exceptions.LocalDBException;
import com.mysql.jdbc.Statement;

public class MainDB {

	private String dbURL = "jdbc:mysql://localhost/";

	private Connection connection;
	private PreparedStatement preparedStatement;

	public boolean init(String dbName, String dbUser, String dbPassword) {
		try {
			Class.forName("com.mysql.jdbc.Driver");

			connection = DriverManager.getConnection(dbURL + dbName, dbUser,
					dbPassword);

		} catch (Exception ex) {
			return false;
		}

		return true;
	}

	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				System.err.println("Error closing DB connection!");
				return;
			}
		}
	}

	public ResultSet fetchRows(String sql) throws SQLException {
		preparedStatement = connection.prepareStatement(sql);
		ResultSet rs = preparedStatement.executeQuery();
		
		return rs;
	}

	public boolean executeSQL(String sql) throws SQLException {
		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.executeUpdate();

		return true;
	}

	public boolean executeRawSQL(String sql) throws SQLException {
		java.sql.Statement stm = connection.createStatement();

		return stm.execute(sql);
	}

	public boolean insert(String tableName, HashMap<String, Object> data)
			throws SQLException {
		String sql = "INSERT FROM " + tableName + " WHERE ";

		preparedStatement = connection.prepareStatement(sql);
		preparedStatement.executeUpdate();

		return true;
	}

}
