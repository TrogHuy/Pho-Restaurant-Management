package TestModel;

import phorestaurant.controller.EmployeeController;
import phorestaurant.model.Employee;

public class TestLogin {
	public static void main(String[] args) {
		System.out.println("--- STARTING LOGIN TEST ---");

        EmployeeController controller = new EmployeeController();

        // TEST 1: Try to login with a fake ID (Should Fail)
        System.out.println("\nTest 1: Invalid User ID");
        boolean result1 = controller.login(999, "Manager"); // ID 999 doesn't exist

        // TEST 2: Try to login with wrong Role (Should Fail)
        // assuming Employee ID 1 exists and is a "Manager"
        System.out.println("\nTest 2: Wrong Role");
        boolean result2 = controller.login(1, "Janitor"); 

        // TEST 3: Correct Login (Should Success)
        // Make sure Employee ID 1 exists in your DB!
        System.out.println("\nTest 3: Correct Credentials");
        boolean result3 = controller.login(2, "Manager"); 

        if (result3) {
            Employee loggedInUser = controller.getCurrentUser();
            System.out.println("Current Session: " + loggedInUser.toString());
        }
	}
}
