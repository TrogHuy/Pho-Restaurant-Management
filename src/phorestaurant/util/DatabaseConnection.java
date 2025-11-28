package phorestaurant.util;

import java.sql.Connection; // Carry query message
import java.sql.DriverManager; // Grant connection to database
import java.sql.SQLException; // Handle errors

public class DatabaseConnection {
	private static final String URL = "jdbc:mysql://localhost:3306/Pho_restaurant";
	private static final String USER = "root";
	private static final String PASSWORD = "123456@";
	
	public static Connection getConnection() {
		try {
			// Load class com.mysql.cj.jdbc.Driver into memory
			Class .forName("com.mysql.cj.jdbc.Driver");
			// Connect to the database with URL, USER and PASSWORD
			return DriverManager.getConnection(URL, USER, PASSWORD);
		} catch (ClassNotFoundException | SQLException e) {
			e.printStackTrace();
			return null;
		}
	}
}
	