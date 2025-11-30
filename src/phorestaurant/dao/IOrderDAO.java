package phorestaurant.dao;

import phorestaurant.model.Order;
import java.util.List;

public interface IOrderDAO {	
	List<Order> getAllOrders();
	Order getOrderById(int id);
	boolean saveOrder(Order order);
	boolean updateOrderStatus(int id, String status);
}
