package phorestaurant.model;

public class MenuItem {
	private int item_id;
	private String name;
	private String description;
	private double price;
	private String category;
	
	// New Item
	public MenuItem(String name, String description, double price, String category) {
		this.name = name;
		this.description = description;
		this.price = price;
		this.category = category;
	}
	
	// Load Item from DB
	public MenuItem(int id, String name, String description, double price, String category) {
		this.item_id = id;
		this.name = name;
		this.description = description;
		this.price = price;
		this.category = category;
	}
	
	// Getters
	public int getID() {
		return this.item_id;
	}
	
	public String getName() {
		return this.name;
	}
	
	public String getDescription() {
		return this.description;
	}
	
	public double getPrice() {
		return this.price;
	}
	
	public String getCategory() {
		return this.category;
	}
	
	// Setters
	public void setID(int id) {
		this.item_id = id;
	}
	public void setName(String name) {
		this.name = name;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public void setPrice(double price) {
		this.price = price;
	}
	
	public void setCategory(String category) {
		this.category = category;
	}
	
	@Override
	public String toString() {
		return this.name + " - " + this.category + " - " + this.price + "\n";
	}
}
