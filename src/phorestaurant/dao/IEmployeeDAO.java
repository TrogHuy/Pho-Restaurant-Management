package phorestaurant.dao;

import phorestaurant.model.Employee;
import java.util.List;

public interface IEmployeeDAO {
	boolean addEmployee(Employee emp);
	boolean deleteEmployee(int id);
	List<Employee> getAllEmployees();
	Employee getEmployeeById(int id);
}
