package phorestaurant.dao;

import phorestaurant.model.*;
import phorestaurant.util.*;
import java.sql.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.ArrayList;
import java.util.List;

public class OrderDAO implements IOrderDAO {
	private static final Logger LOGGER = Logger.getLogger(OrderDAO.class.getName());
	
	@Override
	public boolean saveOrder(Order order) {
		String insertOrderSQL = "INSERT INTO Orders (order_type, order_status, total_price, employee_id) VALUES (?, ?, ?, ?)";
		String insertItemSQL = "INSERT INTO Order_items (order_id, item_id, quantity, subtotal) VALUES (?, ?, ?, ?)";
		Connection conn = null;
		
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			
			PreparedStatement order_stmt = conn.prepareStatement(insertOrderSQL, Statement.RETURN_GENERATED_KEYS);
			order_stmt.setString(1, order.getOrderType());
			order_stmt.setString(2, order.getOrderStatus());
			order_stmt.setDouble(3, order.getTotalPrice());
			order_stmt.setInt(4, order.getEmployeeID());
			
			int rows = order_stmt.executeUpdate();
			if(rows == 0) throw new SQLException("Failed to create order, no rows changed.");
			
			int newOrderId;
			try(ResultSet rs = order_stmt.getGeneratedKeys()) {
				if(rs.next()) {
					newOrderId = rs.getInt(1);
					order.setOrderId(newOrderId);
				}
				else {
					throw new SQLException("Failed to create order, no ID obtained.");
				}
			}
			
			PreparedStatement item_stmt = conn.prepareStatement(insertItemSQL);
			for(OrderItem item : order.getItems()) {
				item_stmt.setInt(1, newOrderId);
				item_stmt.setInt(2, item.getMenuItem().getID());
				item_stmt.setInt(3, item.getQuantity());
				item_stmt.setDouble(4, item.getSubTotal());
				item_stmt.addBatch();
			}
			item_stmt.executeBatch();
			conn.commit();
			return true;
			
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Transaction failed. Rolling back...", e);
			if(conn != null) {
				try {
					conn.rollback();
					LOGGER.info("Rollback complete");
				} catch (SQLException ex) {
					LOGGER.log(Level.SEVERE, "Rollback FAILED! Watchout for Data Integrity consequences!");
				}
			}
			throw new DataAccessException("Failed to save order. Transaction rolled back.", e);
		} 
		
			finally {
				if(conn != null) {
					try {
						conn.setAutoCommit(true);
						conn.close();
					} catch (SQLException e) {e.printStackTrace();}
				}
			}
	}
	
	@Override
	public boolean updateOrderStatus(int id, String status) {
		String sql = "UPDATE Orders SET order_status = ? WHERE order_id = ?";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, status);
			stmt.setInt(2, id);
			return stmt.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error updating status for Order ID: " + id, e);
			throw new DataAccessException("Failed to update order status.", e);
		}
	}
	
	@Override
	public Order getOrderById(int id) {
		Order order = null;
		String order_sql = "SELECT * FROM Orders WHERE order_id = ?";
		String items_sql = "SELECT * FROM Order_items WHERE order_id = ?";
		MenuItemDAO menu_item_dao = new MenuItemDAO();
		
		// main order details
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement order_stmt = conn.prepareStatement(order_sql)) {
			order_stmt.setInt(1, id);
			ResultSet rs = order_stmt.executeQuery();
			
			if(rs.next()) {
				String type = rs.getString("order_type");
				int emp_id = rs.getInt("employee_id");
				
				if(type.equalsIgnoreCase("Grab")) order = new GrabOrder(emp_id);
				else if(type.equalsIgnoreCase("Pickup")) order = new PickUpOrder(emp_id);
				else order = new DineInOrder(emp_id);
			} 
			else return null;
			
			// order items
			try(PreparedStatement item_stmt = conn.prepareStatement(items_sql)) {
				item_stmt.setInt(1, id);
				ResultSet item_rs = item_stmt.executeQuery();
				
				while(item_rs.next()) {
					int menu_item_id = item_rs.getInt("item_id");
                    int quantity = item_rs.getInt("quantity");
                    
                    MenuItem item = menu_item_dao.getItemById(menu_item_id);
                    if (item != null) {
                        order.addItem(new OrderItem(id, item, quantity));
                    }
				}
			}
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error get Order ID: " + id, e);
			throw new DataAccessException("Failed to retrieve order by id.", e);
		}
		return order;
	}
	
	@Override
	public List<Order> getAllOrders() {
	    List<Order> orders = new ArrayList<>();

	    String sql = "SELECT order_id, order_type, order_status, total_price, employee_id " +
	                 "FROM Orders ORDER BY order_id DESC";

	    try (Connection conn = DatabaseConnection.getConnection();
	         PreparedStatement stmt = conn.prepareStatement(sql);
	         ResultSet rs = stmt.executeQuery()) {

	        while (rs.next()) {
	            int id       = rs.getInt("order_id");
	            String type  = rs.getString("order_type");
	            String status= rs.getString("order_status");
	            double total = rs.getDouble("total_price");
	            int empId    = rs.getInt("employee_id");

	            Order o;
	            if (type.equalsIgnoreCase("Grab")) {
	                o = new GrabOrder(empId);
	            } else if (type.equalsIgnoreCase("Pickup")) {
	                o = new PickUpOrder(empId);
	            } else {
	                o = new DineInOrder(empId);
	            }

	            o.setOrderId(id);
	            o.setOrderStatus(status);
	            o.setTotalPrice(total);

	            orders.add(o);
	        }

	    } catch (SQLException e) {
	        LOGGER.log(Level.SEVERE, "Error loading all orders", e);
	        throw new DataAccessException("Failed to retrieve all orders.", e);
	    }

	    return orders;
	}
	
}




