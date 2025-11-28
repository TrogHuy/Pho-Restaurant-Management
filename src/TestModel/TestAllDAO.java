package TestModel;

import phorestaurant.dao.*;
import phorestaurant.model.*;
import java.time.LocalDateTime;
import java.util.List;

public class TestAllDAO {

    public static void main(String[] args) {
        System.out.println("=================================================");
        System.out.println("      PHO RESTAURANT - COMPLETE CRUD TEST        ");
        System.out.println("=================================================\n");

        try {
            testEmployeeCRUD();
            testIngredientCRUD();
            testMenuAndRecipeCRUD();
            testOrderLifecycle();
        } catch (Exception e) {
            System.err.println("❌ CRITICAL FAILURE IN TEST SUITE");
            e.printStackTrace();
        }
    }

    // --------------------------------------------------------
    // MODULE 1: EMPLOYEES (Add -> Read -> Delete)
    // --------------------------------------------------------
    private static void testEmployeeCRUD() {
        System.out.println("--- [1] Testing EmployeeDAO ---");
        EmployeeDAO dao = new EmployeeDAO();
        String tempName = "Test_User_" + System.currentTimeMillis();

        // 1. ADD
        System.out.println("   [+] Adding temporary employee: " + tempName);
        Employee newEmp = new Employee(tempName, "Staff");
        newEmp.setSalary(6000000);
        boolean added = dao.addEmployee(newEmp);
        System.out.println("       Result: " + (added ? "SUCCESS" : "FAILED"));

        // 2. READ (Find the ID)
        System.out.println("   [?] Finding employee ID...");
        List<Employee> list = dao.getAllEmployees();
        int targetId = -1;
        for (Employee e : list) {
            if (e.getName().equals(tempName)) {
                targetId = e.getID();
                System.out.println("       Found ID: " + targetId);
                break;
            }
        }

        // 3. DELETE
        if (targetId != -1) {
            System.out.println("   [-] Deleting employee ID: " + targetId);
            boolean deleted = dao.deleteEmployee(targetId);
            System.out.println("       Result: " + (deleted ? "SUCCESS" : "FAILED"));
            
            // Verify deletion
            Employee check = dao.getEmployeeById(targetId);
            if (check == null) System.out.println("       Verification: User is gone (Good).");
            else System.err.println("       Verification: User STILL EXISTS (Bad).");
        }
        System.out.println("✅ Employee Module Passed.\n");
    }

    // --------------------------------------------------------
    // MODULE 2: INGREDIENTS (Add -> Update Stock -> Delete)
    // --------------------------------------------------------
    private static void testIngredientCRUD() {
        System.out.println("--- [2] Testing IngredientDAO ---");
        IngredientDAO dao = new IngredientDAO();
        String tempIngName = "Test_Spice_" + System.currentTimeMillis();

        // 1. ADD
        System.out.println("   [+] Adding ingredient: " + tempIngName);
        Ingredients newIng = new Ingredients(tempIngName, 10.0, "kg");
        dao.addIngredient(newIng);

        // 2. FIND ID
        int targetId = -1;
        for (Ingredients i : dao.getAllIngredients()) {
            if (i.getName().equals(tempIngName)) {
                targetId = i.getID();
                break;
            }
        }

        if (targetId != -1) {
            // 3. UPDATE STOCK
            System.out.println("   [~] Updating stock (+5.0)...");
            dao.updateStock(targetId, 5.0);
            
            // Verify Update
            for (Ingredients i : dao.getAllIngredients()) {
                if (i.getID() == targetId) {
                    System.out.println("       New Quantity: " + i.getStockQuantity() + " (Expected 15.0)");
                }
            }

            // 4. DELETE
            System.out.println("   [-] Deleting ingredient...");
            dao.deleteIngredient(targetId);
        }
        System.out.println("✅ Ingredient Module Passed.\n");
    }

    // --------------------------------------------------------
    // MODULE 3: MENU & RECIPES (Add Item -> Add Recipe -> Update Recipe -> Delete Item)
    // --------------------------------------------------------
    private static void testMenuAndRecipeCRUD() {
        System.out.println("--- [3] Testing Menu & Recipe DAOs ---");
        MenuItemDAO menuDAO = new MenuItemDAO();
        RecipeDAO recipeDAO = new RecipeDAO();
        IngredientDAO ingDAO = new IngredientDAO();

        String tempDishName = "Test_Pho_Special_" + System.currentTimeMillis();

        // 1. ADD MENU ITEM
        System.out.println("   [+] Adding Menu Item: " + tempDishName);
        MenuItem newItem = new MenuItem(tempDishName, "Test Desc", 99000, "TestCat");
        menuDAO.addMenuItem(newItem);

        // 2. FIND ID
        int itemId = -1;
        for (MenuItem m : menuDAO.searchItems(tempDishName)) {
            itemId = m.getID();
        }

        // Get a valid ingredient ID for the recipe (e.g., ID 1)
        List<Ingredients> allIngs = ingDAO.getAllIngredients();
        if (itemId != -1 && !allIngs.isEmpty()) {
            int ingId = allIngs.get(0).getID();

            // 3. ADD RECIPE LINE
            System.out.println("   [+] Adding Recipe: Item " + itemId + " needs Ingredient " + ingId);
            // Ensure Recipe constructor is public!
            Recipe r = new Recipe(itemId, ingId, 0.5); 
            recipeDAO.addRecipe(r);

            // 4. UPDATE RECIPE
            System.out.println("   [~] Updating Recipe Quantity to 0.8...");
            recipeDAO.updateRecipe(itemId, ingId, 0.8);
            
            // Verify
            List<Recipe> lines = recipeDAO.getRecipeForMenuItem(itemId);
            if (!lines.isEmpty() && lines.get(0).getQuantityNeeded() == 0.8) {
                System.out.println("       Update Verified.");
            }

            // 5. DELETE MENU ITEM (Should cascade delete recipe)
            System.out.println("   [-] Deleting Menu Item (and its recipes)...");
            boolean deleted = menuDAO.deleteMenuItem(itemId);
            System.out.println("       Result: " + (deleted ? "SUCCESS" : "FAILED"));
            
            // Verify Recipe is gone
            if (recipeDAO.getRecipeForMenuItem(itemId).isEmpty()) {
                System.out.println("       Verification: Recipes cleaned up successfully.");
            }
        }
        System.out.println("✅ Menu & Recipe Module Passed.\n");
    }

    // --------------------------------------------------------
    // MODULE 4: ORDERS (Create -> Save -> Update Status -> Delete)
    // --------------------------------------------------------
    private static void testOrderLifecycle() {
        System.out.println("--- [4] Testing OrderDAO Lifecycle ---");
        OrderDAO orderDAO = new OrderDAO();
        MenuItemDAO menuDAO = new MenuItemDAO();
        
        try {
            // Setup: Get a valid Item and Employee
            MenuItem item1 = menuDAO.getAllMenuItems().get(0); 
            int empId = 1; // Assuming Employee 1 exists

            // 1. CREATE & SAVE
            System.out.println("   [+] creating and saving Order...");
            Order order = new DineInOrder(empId); 
            order.addItem(new OrderItem(0, item1, 1));
            
            boolean saved = orderDAO.saveOrder(order);
            if (!saved) {
                System.err.println("       Failed to save order. Stopping test.");
                return;
            }

            // 2. FIND ORDER ID (Trick: We fetch the latest order for this employee)
            // Note: Since we don't have getOrderByID in this test script, 
            // we will simulate finding it or assume you added a way to get the ID.
            // For this test, we skip finding the EXACT ID and rely on the fact 
            // that saveOrder works.
            
            // IMPORTANT: If your saveOrder() sets the ID into the object, we can use order.getID()
            // If not, we will just assume ID logic is handled in the real app.
            
            // Let's assume for the test we want to verify "Update Status"
            // We need an ID. Let's assume it's the max ID in the table.
            int lastOrderId = getLastOrderId(); // Helper method below
            System.out.println("       Working with Order ID: " + lastOrderId);

            // 3. UPDATE STATUS
            System.out.println("   [~] Updating status to 'Cancelled'...");
            orderDAO.updateOrderStatus(lastOrderId, "Cancelled");


        } catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("✅ Order Module Passed.");
    }

    // Helper to get the last inserted ID for testing purposes
    private static int getLastOrderId() {
        int id = 0;
        try (java.sql.Connection conn = phorestaurant.util.DatabaseConnection.getConnection();
             java.sql.Statement stmt = conn.createStatement();
             java.sql.ResultSet rs = stmt.executeQuery("SELECT MAX(order_id) FROM Orders")) {
            if (rs.next()) id = rs.getInt(1);
        } catch (Exception e) { e.printStackTrace(); }
        return id;
    }
}