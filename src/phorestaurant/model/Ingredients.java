package phorestaurant.model;

public class Ingredients {
	private int ingredient_id;
	private String ingredient_name;
	private double stock_quantity;
	private String unit;
	
	// New ingredients
	public Ingredients(String name, double quantity, String unit) {
		this.ingredient_name = name;
		this.stock_quantity = quantity;
		this.unit = unit;
	}
	
	// Load ingredients from DB
	public Ingredients(int id, String name, double stockQuantity, String unit) {
        this.ingredient_id = id;
        this.ingredient_name = name;
        this.stock_quantity = stockQuantity;
        this.unit = unit;
    }

	// Getters
	public int getID() {
		return this.ingredient_id;
	}
	public String getName() {
		return this.ingredient_name;
	}
	public double getStockQuantity() {
		return this.stock_quantity;
	}
	public String getUnit() {
		return this.unit;
	}
	
	// Setters
	public void setID(int id) {
		this.ingredient_id = id;
	}
	public void setName(String name) {
		this.ingredient_name = name;
	}
	
	public void setStockQuantity(double quantity) {
		this.stock_quantity = quantity;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Override
	public String toString() {
		return this.ingredient_name + ": " + this.ingredient_name + "\n"
				+ "In stock: " + this.stock_quantity + " (" + this.unit + ")\n";
	}
}
