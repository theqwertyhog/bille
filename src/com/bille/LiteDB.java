package com.bille;

import java.sql.*;

public class LiteDB {
	
	private byte[] dbPath = {'b', 'i', 'l', 'l', 'e', '.', 'd', 'a', 't'};
	
	private Connection connection;
	private PreparedStatement preparedStatement;

	
	public boolean init() {
		try {
			Class.forName("org.sqlite.JDBC");
			connection = DriverManager.getConnection("jdbc:sqlite:" + new String(dbPath));
		} catch (Exception e) {
			return false;
		}
		
		return true;
	}
	
	public void close() {
		if (connection != null) {
			try {
				connection.close();
			} catch (SQLException e) {
				System.err.println("Error closing local DB connection!");
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

}