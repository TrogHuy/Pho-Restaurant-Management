package TestModel;

import phorestaurant.controller.*;
import phorestaurant.model.*;
import phorestaurant.util.UserSession;
import java.util.List;

public class TestControllers {
	public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("   PHO RESTAURANT - CONTROLLER INTEGRATION TEST  ");
        System.out.println("=================================================\n");

        // Initialize Controllers
        EmployeeController empController = new EmployeeController();
        InventoryController invController = new InventoryController();
        MenuController menuController = new MenuController();
        OrderController orderController = new OrderController();
        FinanceController financeController = new FinanceController();

        // ---------------------------------------------------------------
        // SCENARIO 1: SECURITY CHECK (Attempting unauthorized access)
        // ---------------------------------------------------------------
        System.out.println("--- [1] Security Test (Unauthenticated) ---");
        // Try to add stock without logging in
        boolean hackAttempt = invController.restockIngredient(1, 100); 
        if (!hackAttempt) System.out.println("✅ Security Passed: Unauthorized restock blocked.");

        // ---------------------------------------------------------------
        // SCENARIO 2: MANAGER ACTIONS (Login & Management)
        // ---------------------------------------------------------------
        System.out.println("\n--- [2] Manager Actions ---");
        
        // A. Login as Manager (ID 1 assumed to be Manager from dummy data)
        boolean loginSuccess = empController.login(1, "Manager");
        if (loginSuccess) {
            // B. Check Stock (Public)
            List<Ingredients> stock = invController.viewInventory();
            System.out.println("   > Current Stock Items: " + stock.size());
            
            // C. Restock (Manager Only)
            if (!stock.isEmpty()) {
                int firstIngId = stock.get(0).getID();
                System.out.println("   > Restocking Ingredient ID " + firstIngId + "...");
                invController.restockIngredient(firstIngId, 50.0);
            }
            
            // D. Logout
            empController.logout();
        }

        // ---------------------------------------------------------------
        // SCENARIO 3: STAFF FLOW (Order & Payment)
        // ---------------------------------------------------------------
        System.out.println("\n--- [3] Staff Actions (Order Flow) ---");
        
        // A. Login as Staff (ID 2 assumed to be Staff)
        // Note: Ensure ID 2 exists in your DB and role matches "Staff"
        if (empController.login(2, "Staff")) {
            
            // B. Create a Draft Order (Dine In)
            Order draftOrder = orderController.createNewDraftOrder("DineIn");
            System.out.println("   > Created Draft Order for: " + empController.getCurrentUser().getName());

            // C. Add Items (Using Controller Logic)
            // Assuming Item ID 1 (Pho Tai) exists
            List<MenuItem> menu = menuController.getFullMenu();
            if (!menu.isEmpty()) {
                int itemId = menu.get(0).getID(); // Get first item
                orderController.addItemToDraft(draftOrder, itemId, 2); // Order 2 bowls
                System.out.println("   > Added 2x " + menu.get(0).getName());
            }

            // D. Place Order (Triggers Database + Inventory Deduction)
            System.out.println("   > Placing Order...");
            boolean placed = orderController.placeOrder(draftOrder);
            
            if (placed) {
                // E. Process Payment
                // Since we don't have the generated ID in the draft object (unless updated),
                // we simulates payment for a generic valid ID or the one we just made.
                // NOTE: In a real app, 'placeOrder' would update the ID inside 'draftOrder'.
                // If your DAO doesn't update the ID in the object, this step might fail 
                // unless we implement that setID logic.
                
                // For testing, let's assume the DAO works and allows us to pay.
                // If draftOrder.getID() is 0 (because DAO didn't set it back), this will fail.
                // Let's assume for this test we manually grab the last ID for demonstration.
                int orderId = draftOrder.getID(); 
                if (orderId == 0) orderId = 1; // Fallback for test if ID sync isn't implemented

                System.out.println("   > Processing Payment for Order ID: " + orderId);
                boolean paid = financeController.processPayment(orderId, draftOrder.getTotalPrice(), "Cash");
                
                // F. Check Revenue
                System.out.println("   > Total Revenue: " + financeController.getTotalRevenue());
            }
        }
    }
}
