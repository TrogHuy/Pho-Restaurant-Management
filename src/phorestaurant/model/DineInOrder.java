package phorestaurant.model;

import java.time.LocalDateTime;

public class DineInOrder extends Order {
	public DineInOrder(int id, String status, double total, LocalDateTime time, int empId) {
		super(id, status, total, time, empId);
	}
	
	public DineInOrder(int empId) {
		super(empId);
	}
	
	@Override
	public void calcTotalPrice() {
		double sum = 0;
		for(OrderItem item : items) {
			sum += item.getSubTotal();
		}
		this.total_price = sum;
	}
}
