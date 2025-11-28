package phorestaurant.controller;

import phorestaurant.model.*;
import phorestaurant.util.UserSession;
import phorestaurant.dao.*;
import java.util.List;


public class OrderController {
	private OrderDAO order_dao;
	private IngredientDAO ingredient_dao;
	private RecipeDAO recipe_dao;
	private MenuItemDAO menu_item_dao;
	
	public OrderController() {
		this.order_dao = new OrderDAO();
		this.ingredient_dao = new IngredientDAO();
		this.recipe_dao = new RecipeDAO();
		this.menu_item_dao = new MenuItemDAO();
	}
	
	public Order createNewDraftOrder(String type) {
		Employee current_user = UserSession.getInstance().getCurrentUser();
		int emp_id = (current_user != null ) ? current_user.getID() : 0;
		
		if(type.equalsIgnoreCase("Grab"))
			return new GrabOrder(emp_id);
		else if(type.equalsIgnoreCase("Pickup"))
			return new PickUpOrder(emp_id);
		else
			return new DineInOrder(emp_id);
	}
	
	public void addItemToDraft(Order order, int item_id, int quantity) {
		if(quantity <= 0) return;
		
		MenuItem item = menu_item_dao.getItemById(item_id);
		if(item != null) {
			OrderItem line_item = new OrderItem(0, item, quantity);
			order.addItem(line_item);
		}
	}
	
	public boolean placeOrder(Order order) {
		if(order.getItems().isEmpty()) {
			System.out.println("Error: Cannot place an empty order.");
			return false;
		}
		
		boolean save_success = order_dao.saveOrder(order);
		if(save_success) {
			System.out.println("Order saved successfully.");
			updateInventoryForOrder(order, true);
			return true;
		}
		else {
			System.out.println("Failed to save order.");
			return false;
		}
	}
	
	private void updateInventoryForOrder(Order order, boolean is_selling) {
		for(OrderItem order_item : order.getItems()) {
			int menu_item_id = order_item.getMenuItem().getID();
			int qty_sold = order_item.getQuantity();
			
			List<Recipe> recipe_lines = recipe_dao.getRecipeForMenuItem(menu_item_id);
			
			for(Recipe line : recipe_lines) {
				int ingredient_id = line.getIngredientID();
				double qty_needed = line.getQuantityNeeded();
				
				double total_change = qty_needed * qty_sold;
				
				if(is_selling) 
					total_change *= -1;
				
				ingredient_dao.updateStock(ingredient_id, total_change);
			}
		}
	}
	
	public boolean completeOrder(int id) {
		return order_dao.updateOrderStatus(id, "Completed");
	}
	
	public boolean cancelOrder(int id) {
		Order existing_order = order_dao.getOrderById(id);
		
		if(existing_order == null) {
			System.out.println("Error: Order ID: " + id + " not found.");
			return false;
		}
		
		if(existing_order.getOrderStatus().equalsIgnoreCase("Cancelled")) {
			System.out.println("Order is already cancelled");
			return false;
		}
		
		boolean updated = order_dao.updateOrderStatus(id, "Cancelled");
		
		if(updated) {
			updateInventoryForOrder(existing_order, true);
			System.out.println("Order cancelled and inventory restored.");
			return true;
		}
		return false;
	}
}
