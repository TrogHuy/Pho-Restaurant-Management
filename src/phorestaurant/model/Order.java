package phorestaurant.model;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public abstract class Order {
	protected int order_id;
	protected String order_status;
	protected double total_price;
	protected LocalDateTime order_timestamp;
	protected int employee_id;
	protected List<OrderItem> items = new ArrayList<>();
	
	public Order(int id, String status, double price, LocalDateTime time, int employee_id) {
		this.order_id = id;
		this.order_status = status;
		this.total_price = price;
		this.order_timestamp = time;
		this.employee_id = employee_id;
	}
	
	// Create new order in GUI
	public Order(int employeeId) {
		this.order_timestamp = LocalDateTime.now();
		this.order_status = "Pending";
		this.employee_id = employeeId;
	}
	
	public abstract void calcTotalPrice();
	
	public void addItem(OrderItem item) {
		items.add(item);
		calcTotalPrice();
	}
	
	// Getters
	public int getID() {
		return this.order_id;
	}
	public String getOrderType() {
		return this.getClass().getSimpleName().replace("Order", "");
	}
	public String getOrderStatus() {
		return this.order_status;
	}
	public double getTotalPrice() {
		return this.total_price;
	}
	public LocalDateTime getTime() {
		return this.order_timestamp;
	}
	public int getEmployeeID() {
		return this.employee_id;
	}

	
	// Setters

	public List<OrderItem> getItems() {
	    return this.items;
	}
	
	// Setters
	public void setOrderId(int id) {
		this.order_id = id;
	}

	public void setOrderStatus(String status) {
		this.order_status = status;
	}
	public void setTime(LocalDateTime time) {
		this.order_timestamp = time;
	}
	public void assignEmployee(int id) {
		this.employee_id = id;
	}
}