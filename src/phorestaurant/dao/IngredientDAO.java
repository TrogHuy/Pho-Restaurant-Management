package phorestaurant.dao;

import phorestaurant.model.*;
import phorestaurant.util.*;
import java.sql.*;
import java.util.List;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.logging.Level;

public class IngredientDAO {
	private static final Logger LOGGER = Logger.getLogger(IngredientDAO.class.getName());
	
	public List<Ingredients> getAllIngredients() {
		List<Ingredients> list = new ArrayList<>();
		String sql = "SELECT * FROM Ingredients";
		
		try(Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			while(rs.next()) {
				list.add(new Ingredients(
					rs.getInt("ingredient_id"),
					rs.getString("name"),
					rs.getDouble("stock_quantity"),
					rs.getString("unit")
					));
			} 
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error fetching all ingredients.", e);
			throw new DataAccessException("Could not retrieve ingredients from the database.", e);
		}
		return list;
	}
	
	public boolean updateStock(int id, double changed_quantity) {
		String sql = "UPDATE Ingredients SET stock_quantity = stock_quantity + ? WHERE ingredient_id = ?";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, changed_quantity);
			stmt.setInt(2, id);
			
			int rows = stmt.executeUpdate();
			
			if(rows == 0) LOGGER.log(Level.WARNING, "Stock update failed: Cannot find Ingredient ID: " + id);
			return rows > 0;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to update stock for Ingredient ID: " + id, e);
			throw new DataAccessException("Failed to update stock inventory.", e);
		}
	}
	
	public boolean isStockLow(int ingredient_id, double threshold) {
		String sql = "SELECT stock_quantity FROM Ingredients WHERE ingredient_id = ?";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, ingredient_id);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) return rs.getDouble("stock_quantity") < threshold;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error checking stock quantity.", e);
			throw new DataAccessException("Failed to check stock quantity", e);
		}
		return false;
	}
	
	public boolean addIngredient(Ingredients ingredient) {
		String sql = "INSERT INTO Ingredients (ingredient_id, name, stock_quantity, unit) VALUES (?,?,?,?)";
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, ingredient.getID());
			stmt.setString(2, ingredient.getName());
			stmt.setDouble(3, ingredient.getStockQuantity());
			stmt.setString(4, ingredient.getUnit());

			return stmt.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error: failed to insert ingredient: " + ingredient.getName(), e);
			throw new DataAccessException("Failed to insert new ingredient.", e);
		}
	}
	
	public boolean deleteIngredient(int id) {
		String delete_fk_sql = "DELETE FROM Recipes WHERE ingredient_id = ?";
		String delete_target_sql = "DELETE FROM Ingredients WHERE ingredient_id = ?";
		
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			
			try (PreparedStatement stmt = conn.prepareStatement(delete_fk_sql)) {
                stmt.setInt(1, id);
                stmt.executeUpdate();
            }
			
			int rows = 0;
			try (PreparedStatement stmt = conn.prepareStatement(delete_target_sql)) {
                stmt.setInt(1, id);
                rows = stmt.executeUpdate();
            }
			
			conn.commit();
			return rows > 0;
			
		} catch(SQLException e) {
			if(conn != null) {
				try {conn.rollback();}
				catch(SQLException ex) {ex.printStackTrace();}
			}
			LOGGER.log(Level.SEVERE, "Error deleting ingredient id: " + id, e);
			throw new DataAccessException("Failed to delete ingredient.", e);
		}
		
		finally {
			if(conn != null) {
				try {conn.setAutoCommit(true); conn.close();}
				catch(SQLException e) {e.printStackTrace();}
			}
		}
	}
}
