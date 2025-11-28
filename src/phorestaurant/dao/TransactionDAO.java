package phorestaurant.dao;

import phorestaurant.util.*;
import phorestaurant.model.Transaction;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;

public class TransactionDAO {
	private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());
	
	public boolean saveTransaction(Transaction trans) {
		String sql = "INSERT INTO Transactions (order_id, payment_method, amount_paid, transaction_timestamp)"
				+ " VALUES (?, ?, ?, ?)";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, trans.getOrderId());
			stmt.setString(2, trans.getPaymentMethod());
			stmt.setDouble(3, trans.getAmountPaid());
			stmt.setTimestamp(4, Timestamp.valueOf(trans.getTimestamp()));
			return stmt.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error saving transaction for Transaction ID: " + trans.getOrderId(), e);
			throw new DataAccessException("Payment failed. Transaction not saved.", e);
		}
	}
	
	public double getTotalRevenue() {
		String sql = "SELECT Sum(amount_paid) FROM transactions";
		
		try(Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			if(rs.next())
				return rs.getDouble(1);
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error fetching amount_paid from transactions.", e);
			throw new DataAccessException("Cannot retrieve total revenue from the database", e);
		}
		return 0.0;
	}
}