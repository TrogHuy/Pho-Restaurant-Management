package phorestaurant.model;

import java.time.LocalDateTime;

public class Transaction {
	private int transaction_id;
	private int order_id;
	private String payment_method;
	private double amount_paid;
	private LocalDateTime transaction_timestamp;
	
	// Load transaction from DB
	public Transaction(int trans_id, int order_id, String method, double amt_paid, LocalDateTime time) {
		this.transaction_id = trans_id;
		this.order_id = order_id;
		this.payment_method = method;
		this.amount_paid = amt_paid;
		this.transaction_timestamp = time;
	}
	
	// New transaction
	public Transaction(int order_id, String method, double amt_paid, LocalDateTime time) {
		this.order_id = order_id;
		this.payment_method = method;
		this.amount_paid = amt_paid;
		this.transaction_timestamp = time;
	}
	
	// Getters
    public int getTransactionId() { 
    	return this.transaction_id; 
    }
    
    public int getOrderId() { 
    	return this.order_id; 
    }
  
    public String getPaymentMethod() { 
    	return this.payment_method; 
    }
    
    public double getAmountPaid() { 
    	return this.amount_paid; 
    }
    
    public LocalDateTime getTimestamp() { 
    	return this.transaction_timestamp; 
    }
    
    // Setters
    public void setTransId(int id) {
    	this.transaction_id = id;
    }
    
    public void setPaymentMethod(String method) {
    	this.payment_method = method;
    }
    
    public void setTransactionTime(LocalDateTime time) {
    	this.transaction_timestamp = time;
    }
}

