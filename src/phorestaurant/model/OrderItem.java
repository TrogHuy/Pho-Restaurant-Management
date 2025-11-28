package phorestaurant.model;

public class OrderItem {
	private int order_id;
	private MenuItem menuItem;
	private int quantity;
	private double sub_total;
	
	// Constructor for new order item
	public OrderItem(int order_id, MenuItem menuItem, int quantity) {
		this.order_id = order_id;
		this.menuItem = menuItem;
		this.quantity = quantity;
		this.sub_total = menuItem.getPrice() * quantity;
	}
	
	// Constructor for loading DB (record sub_total from history)
	public OrderItem(int order_id, MenuItem menuItem, int quantity, double subTotalSoFar) {
		this.order_id = order_id;
		this.menuItem = menuItem;
		this.quantity = quantity;
		this.sub_total = subTotalSoFar;
	}
	
	// Getters
	public int getOrderID() {
		return this.order_id;
	}
	public int getQuantity() {
		return this.quantity;
	}
	public double getSubTotal() {
		return this.sub_total;
	}
	public MenuItem getMenuItem() {
		return this.menuItem;
	}
	
	// Setters
	public void setQuantity(int quantity) {
        if (quantity <= 0) {
            throw new IllegalArgumentException("Quantity must be positive");
        }
        this.quantity = quantity;
        // update new sub_total
        this.sub_total = this.menuItem.getPrice() * quantity;
    }
}
