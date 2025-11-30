package phorestaurant.controller;

import phorestaurant.dao.TransactionDAO;
import phorestaurant.model.Transaction;
import phorestaurant.util.UserSession;

import java.time.LocalDateTime;
import java.util.List;

public class FinanceController {
	private TransactionDAO transaction_dao;
	
	public FinanceController() {
		transaction_dao = new TransactionDAO();
	}
	
	public boolean processPayment(int order_id, double amt_paid, String method) {
		if(amt_paid <= 0) {
			System.out.println("Error: Payment must be postive.");
			return false;
		}
		
		if(order_id <= 0) {
			System.out.println("Error: Invalid order id.");
			return false;
		}
		
		Transaction trans = new Transaction(order_id, method, amt_paid, LocalDateTime.now());
		
		boolean success = transaction_dao.saveTransaction(trans);
		if(success) System.out.println("Received payment successfully via " + method);
		else System.out.println("Payment process failed.");
		return success;
	}
	
	public double getTotalRevenue() {
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can view financial reports.");
			return -1;
		}
		return transaction_dao.getTotalRevenue();
	}
	
	public List<Transaction> getAllTransactions() {
	    return transaction_dao.getAllTransactions();
	}
}