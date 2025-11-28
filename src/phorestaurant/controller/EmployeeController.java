package phorestaurant.controller;

import phorestaurant.dao.EmployeeDAO;
import phorestaurant.model.Employee;
import java.util.List;
import phorestaurant.util.UserSession;

public class EmployeeController {
	private EmployeeDAO employee_dao;
	
	public EmployeeController() {
		this.employee_dao = new EmployeeDAO();
	}
	
	public boolean login(int id, String role) {
		Employee emp = employee_dao.getEmployeeById(id);
		
		if(emp == null) {
			System.out.println("Failed login: USER ID " + id + " not found.");
			return false;
		}
		
		if(!emp.getRole().equalsIgnoreCase(role)) {
			System.out.println("Failed login: Unmatched role.");
			return false;
		}
		
		UserSession.getInstance().setCurrentUser(emp);
		System.out.println("Login success! User: " + emp.getName() + " (" + emp.getRole() + ")");
		return true;
	}
	
	public Employee getCurrentUser() {
		return UserSession.getInstance().getCurrentUser();
	}
	
	public void logout() {
		UserSession.getInstance().logout();
		System.out.println("Logged out.");
	}
	
	public boolean addNewEmployee(String name, String role, int salary) {
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can add new staff.");
			return false;
		}
		
		if(salary < 100000) {
			System.out.println("Salary must greater than 100,000 VND.");
		}
		
		Employee new_emp = new Employee(name, role);
		new_emp.setSalary(salary);
		
		return employee_dao.addEmployee(new_emp);
	}
	
	public boolean deleteEmployee(int id) {
		if(UserSession.getInstance() == null) {
			System.out.println("Error: Please login to perform this action.");
			return false;
		}
		
		if(!UserSession.getInstance().isManager()) {
			System.out.println("Access denied: Only managers can delete employees.");
			return false;
		}
		
		if(UserSession.getInstance().getCurrentUser().getID() == id) {
			System.out.println("Error: Cannot delete your own account while logged in.");
			return false;
		}
		
		return employee_dao.deleteEmployee(id);
	}
	
	public List<Employee> viewAllEmployees() {
		return employee_dao.getAllEmployees();
	}
}
