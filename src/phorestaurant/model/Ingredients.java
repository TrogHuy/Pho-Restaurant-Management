package phorestaurant.model;

public class Ingredients {
	private int ingredient_id;
	private String name;
	private double stock_quantity;
	private String unit;
	
	// New ingredients
	public Ingredients(String name, double quantity, String unit) {
		this.name = name;
		this.stock_quantity = quantity;
		this.unit = unit;
	}
	
	// Load ingredients from DB
	public Ingredients(int id, String name, double stockQuantity, String unit) {
        this.ingredient_id = id;
        this.name = name;
        this.stock_quantity = stockQuantity;
        this.unit = unit;
    }

	// Getters
	public int getID() {
		return this.ingredient_id;
	}
	public String getName() {
		return this.name;
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
		this.name = name;
	}
	
	public void setStockQuantity(double quantity) {
		this.stock_quantity = quantity;
	}
	
	public void setUnit(String unit) {
		this.unit = unit;
	}
	
	@Override
	public String toString() {
		return this.name + ": " + this.name + "\n"
				+ "In stock: " + this.stock_quantity + " (" + this.unit + ")\n";
	}
}