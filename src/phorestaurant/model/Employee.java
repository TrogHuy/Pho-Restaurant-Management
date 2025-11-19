package phorestaurant.model;

public class Employee {
	private int employee_id;
	private String role;
	private String full_name;
	private int salary;
	
	public Employee(String full_name, String role, int salary) {
		this.full_name = full_name;
		this.role = role; 
		this.salary = salary;
	}
	
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
	
	void setRole(String role) {
		this.role = role;
	}
	
	void setName(String name) {
		this.full_name = name;
	}
	
	void setSalary(int salary) {
		this.salary = salary;
	}
}
