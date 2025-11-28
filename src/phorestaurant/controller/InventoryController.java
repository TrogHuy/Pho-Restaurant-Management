package phorestaurant.controller;

import phorestaurant.dao.IngredientDAO;
import phorestaurant.model.Ingredients;
import phorestaurant.util.UserSession;
import java.util.List;

public class InventoryController {
	private IngredientDAO ingredient_dao;
	
	public InventoryController() {
		ingredient_dao = new IngredientDAO();
	}
	
	public List<Ingredients> viewInventory() {
		return ingredient_dao.getAllIngredients();
	}
	
	public boolean checkLowStock(int id, double threshold) {
		return ingredient_dao.isStockLow(id, threshold);
	}
	
	public boolean addNewIngredient(String name, Double qty, String unit) {
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can add new ingredients.");
			return false;
		}
		
		if(qty < 0) return false;
		
		Ingredients new_ingredient = new Ingredients(name, qty, unit);
		return ingredient_dao.addIngredient(new_ingredient);
	}
	
	public boolean deleteIngredient(int id) {
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can delete ingredients.");
			return false;
		}
		
		return ingredient_dao.deleteIngredient(id);
	}
	
	public boolean restockIngredient(int id, double amount) {
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can restock inventory.");
			return false;
		}
		return ingredient_dao.updateStock(id, amount);
	}
}
