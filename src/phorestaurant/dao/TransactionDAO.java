package phorestaurant.dao;

import phorestaurant.util.*;
import phorestaurant.model.Transaction;
import java.sql.*;
import java.time.LocalDateTime;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO implements ITransactionDAO {
	private static final Logger LOGGER = Logger.getLogger(TransactionDAO.class.getName());
	
	@Override
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
	
	@Override
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
	
	@Override
	public List<Transaction> getAllTransactions() {
	    String sql = "SELECT transaction_id, order_id, payment_method, amount_paid, transaction_timestamp "
	               + "FROM transactions ORDER BY transaction_id";

	    List<Transaction> list = new ArrayList<>();

	    try (Connection conn = DatabaseConnection.getConnection();
	         Statement stmt = conn.createStatement();
	         ResultSet rs = stmt.executeQuery(sql)) {

	        while (rs.next()) {
	            int transId = rs.getInt("transaction_id");
	            int orderId = rs.getInt("order_id");
	            String method = rs.getString("payment_method");
	            double amount = rs.getDouble("amount_paid");
	            LocalDateTime ts = rs.getTimestamp("transaction_timestamp").toLocalDateTime();

	            // ⚠️ adjust this part to match your Transaction class
	            Transaction t = new Transaction(orderId, method, amount, ts);
	            t.setTransId(transId);   // or use another constructor if you have one

	            list.add(t);
	        }
	    } catch (SQLException e) {
	        LOGGER.log(Level.SEVERE, "Error fetching transactions", e);
	        throw new DataAccessException("Cannot retrieve transactions list", e);
	    }
	    return list;
	}

}