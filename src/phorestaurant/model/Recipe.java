package phorestaurant.model;

public class Recipe {
	private int item_id;
	private int ingredient_id;
	private double quantity_needed;
	
	public Recipe(int item_id, int ingredient_id, double quantity) {
		this.item_id = item_id;
		this.ingredient_id = ingredient_id;
		this.quantity_needed = quantity;
	}
	
	// Getters
	public int getItemID() {
		return this.item_id;
	}
	public int getIngredientID() {
		return this.ingredient_id;
	}
	public double getQuantityNeeded() {
		return this.quantity_needed;
	}
	
	// Setters
	public void setQuantityNeeded(double quantity) {
		this.quantity_needed = quantity;
	}
}
