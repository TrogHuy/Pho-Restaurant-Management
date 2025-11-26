package phorestaurant.model;

import java.time.LocalDateTime;

public class PickUpOrder extends Order {
	public PickUpOrder(int id, String status, double total, LocalDateTime time, int empId) {
		super(id, status, total, time, empId);
	}
	
	public PickUpOrder(int empId) {
		super(empId);
	}
	
	@Override
	public void calcTotalPrice() {
		double sum = 0;
		for(OrderItem item : items) {
			sum += item.getSubTotal();
		}
		this.total_price = sum + 5000; // To-go box fee
	}
}
