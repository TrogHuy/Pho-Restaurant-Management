package phorestaurant.dao;

import phorestaurant.model.Employee;
import phorestaurant.util.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

public class EmployeeDAO {
	private static final Logger LOGGER = Logger.getLogger(EmployeeDAO.class.getName());
	
	public Employee getEmployeeById(int id) {
		String sql = "SELECT * FROM Employees WHERE employee_id = ?";
		try ( Connection conn = DatabaseConnection.getConnection(); 
				PreparedStatement stmt = conn.prepareStatement(sql) ) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return new Employee(
						rs.getInt("employee_id"),
						rs.getString("full_name"),
						rs.getString("role"),
						rs.getInt("salary")
				);
			}
		} catch (SQLException e) {
			// developer
			LOGGER.log(Level.SEVERE, "Failed to fetch employee with ID: " + id, e);
			// throw error for Controller
			throw new DataAccessException("Could not retrieve specific employee from the database", e);
		}
		return null;
	}
	
	public boolean addEmployee(Employee emp) {
		String sql = "INSERT INTO Employees (full_name, role, salary) VALUES (?, ?, ?)";
		try ( Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {

			stmt.setString(1, emp.getName());
			stmt.setString(2, emp.getRole());
			stmt.setInt(3, emp.getSalary());
			
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error loading new employee: " + emp.getName(), e);
			throw new DataAccessException("Failed to insert new employee to database.", e);
		}
	}
	
	public boolean deleteEmployee(int id) {
		String unlink_sql = "UPDATE ORDERS SET employee_id = NULL WHERE employee_id = ?";
		String delete_sql = "DELETE FROM Employees WHERE employee_id = ?";
		
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			
			try(PreparedStatement unlink_stmt = conn.prepareStatement(unlink_sql)) {
				unlink_stmt.setInt(1, id);
				unlink_stmt.executeUpdate();
			}
			
			
			int rows_deleted;
			try(PreparedStatement delete_stmt = conn.prepareStatement(delete_sql)) {
				delete_stmt.setInt(1, id);
				rows_deleted = delete_stmt.executeUpdate();
			}
			
			conn.commit();
			return rows_deleted > 0;
			
		} catch(SQLException e) {
			if(conn != null) {
				try {conn.rollback();}
				catch(SQLException ex) {
					LOGGER.log(Level.SEVERE, "Error rolling back!!!", ex);
					throw new DataAccessException("Failed to roll back.", ex);
				}
			}
			LOGGER.log(Level.SEVERE, "Error deleting Employee ID: " + id, e);
			throw new DataAccessException("Failed to delete employee.", e);
		}
		
		finally {
			try {
				if(conn != null) {
					conn.setAutoCommit(true);
					conn.close();
				}
			} catch(SQLException e) {e.printStackTrace();}
		}
	}
	
	public List<Employee> getAllEmployees() {
		List<Employee> list = new ArrayList<>();
		String sql = "SELECT * FROM Employees";
		try( Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				list.add(new Employee(
				rs.getInt("employee_id"),
				rs.getString("full_name"),
				rs.getString("role"),
				rs.getInt("salary")
				));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error fetching all employees.", e);
			throw new DataAccessException("Could not retrieve employees from the database.", e);
		}
		return list;
	}
}
