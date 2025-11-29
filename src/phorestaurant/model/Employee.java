package phorestaurant.model;

public class Employee {
	private int employee_id;
	private String role;
	private String full_name;
	private int salary;
	
	// New employee
	public Employee(String full_name, String role) {
		this.full_name = full_name;
		this.role = role;
	}
	
	// Load employee from DB
	public Employee(int id, String full_name, String role, int salary) {
		this.full_name = full_name;
		this.role = role; 
		this.salary = salary;
		this.employee_id = id;
	}
	
	// Getters
	public int getID() {
		return this.employee_id;
	}
	
	public String getRole() {
		return this.role;
	}
	
	public String getName() {
		return this.full_name;
	}
	
	public int getSalary() {
		return this.salary;
	}
	
	// Setters
	public void setId(int id) {
		this.employee_id = id;
	}
	public void setRole(String role) {
		this.role = role;
	}
	
	public void setName(String name) {
		this.full_name = name;
	}
	
	public void setSalary(int salary) {
		this.salary = salary;
	}
	
	@Override
	public String toString() {
		return "Employee name: " + this.full_name + " - " + "Role: " + this.role + " - " + "Salary: " + this.salary + "\n";
	}
}


