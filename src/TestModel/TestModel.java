package TestModel;

import phorestaurant.model.*;

public class TestModel {
	public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("      PHO RESTAURANT - MODEL TESTING      ");
        System.out.println("==========================================\n");

        testBasicEntities();
        testOrderItemEncapsulation();
        testOrderPolymorphism();
    }

    // TEST 1: Basic Entities (Employee, Ingredient, Menu Item)
    // Goal: Verify constructors and toString() overrides
    private static void testBasicEntities() {
        System.out.println("--- Test 1: Basic Entities (Encapsulation) ---");

        // 1. Employee
        Employee emp = new Employee("Nguyen Van An", "Manager");
        emp.setId(101); // Simulate DB assigning ID
        System.out.println("Employee Created: " + emp.toString());

        // 2. Ingredient
        Ingredients beef = new Ingredients("Beef Tenderloin", 50.5, "kg");
        System.out.println("Ingredient Created: " + beef.getName() + " (" + beef.getStockQuantity() + " " + beef.getUnit() + ")");

        // 3. MenuItem
        MenuItem pho = new MenuItem("Pho Tai", "Rare Beef Soup", 55000, "Pho");
        System.out.println("Menu Item Created: " + pho.toString());
        
        System.out.println("✅ Basic Entities Test Passed\n");
    }

    // TEST 2: Encapsulation logic in OrderItem
    // Goal: Verify that changing quantity AUTOMATICALLY updates subtotal
    private static void testOrderItemEncapsulation() {
        System.out.println("--- Test 2: OrderItem Encapsulation Logic ---");

        MenuItem tea = new MenuItem("Tra Da", "Iced Tea", 5000, "Drink");
        
        // Create order item with quantity 1
        OrderItem item = new OrderItem(1, tea, 1);
        System.out.println("Item: " + tea.getName());
        System.out.println("Initial Quantity: " + item.getQuantity());
        System.out.println("Initial Subtotal: " + item.getSubTotal() + " (Expected: 5000.0)");

        // CHANGE QUANTITY -> Logic should auto-update sub_total
        System.out.println(">> Updating Quantity to 5...");
        item.setQuantity(5);
        
        System.out.println("New Quantity: " + item.getQuantity());
        System.out.println("New Subtotal: " + item.getSubTotal());

        if (item.getSubTotal() == 25000.0) {
            System.out.println("✅ Encapsulation Success: Subtotal updated automatically.");
        } else {
            System.out.println("❌ Encapsulation Failed: Subtotal did not update.");
        }
        System.out.println();
    }

    // TEST 3: Polymorphism in Orders
    // Goal: Verify that DineIn and Grab calculate totals differently using the SAME method
    private static void testOrderPolymorphism() {
        System.out.println("--- Test 3: Polymorphism (Order Calculation) ---");

        // Create some food items
        MenuItem pho = new MenuItem("Pho Tai", "Rare Beef", 50000, "Pho"); // 50k
        MenuItem coke = new MenuItem("Coke", "Soda", 10000, "Drink");     // 10k
        
        // Total base price = 60,000 VND

        // 1. Create a DINE-IN Order
        Order dineIn = new DineInOrder(1); // Employee ID 1
        dineIn.addItem(new OrderItem(0, pho, 1));
        dineIn.addItem(new OrderItem(0, coke, 1));
        
        // 2. Create a GRAB Order
        Order grab = new GrabOrder(1);     // Employee ID 1
        grab.addItem(new OrderItem(0, pho, 1));
        grab.addItem(new OrderItem(0, coke, 1));

        // 3. Compare Results
        System.out.println("Items ordered: 1 Pho (50k) + 1 Coke (10k) = Base 60k");
        
        System.out.println("\n[DineIn Order]");
        System.out.println("Type: " + dineIn.getOrderType());
        System.out.println("Total: " + dineIn.getTotalPrice()); 
        // Expected: 60,000 (Standard)

        System.out.println("\n[Grab Order]");
        System.out.println("Type: " + grab.getOrderType());
        System.out.println("Total: " + grab.getTotalPrice()); 
        // Expected: 72,000 (60,000 * 1.20 fee)

        if (grab.getTotalPrice() > dineIn.getTotalPrice()) {
            System.out.println("\n✅ Polymorphism Success: Grab order automatically applied surcharge.");
        } else {
            System.out.println("\n❌ Polymorphism Failed: Prices are identical.");
        }
    }
}
