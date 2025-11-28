package phorestaurant.dao;

import phorestaurant.model.Recipe;
import phorestaurant.util.*;
import java.sql.*;
import java.util.logging.Logger;
import java.util.logging.Level;
import java.util.List;
import java.util.ArrayList;


public class RecipeDAO {
	public static final Logger LOGGER = Logger.getLogger(RecipeDAO.class.getName());
	
	public List<Recipe> getRecipeForMenuItem(int item_id) {
		String sql = "SELECT * FROM Recipes WHERE item_id = ?";
		List<Recipe> recipe_list = new ArrayList<>();
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, item_id);
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				recipe_list.add(new Recipe(
						rs.getInt("item_id"),
						rs.getInt("ingredient_id"),
						rs.getDouble("quantity_needed")
				));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to retrieve recipe for Item ID: " + item_id, e);
			throw new DataAccessException("Failed to load recipe details.");
		}
		return recipe_list;
	}
	
	public boolean addRecipe(Recipe recipe) {
		String sql = "INSERT INTO Recipes (item_id, ingredient_id, quantity_needed) VALUES (?, ?, ?)";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, recipe.getItemID());
			stmt.setInt(2, recipe.getIngredientID());
			stmt.setDouble(3, recipe.getQuantityNeeded());
			
			return stmt.executeUpdate() > 0;
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Error adding new recipe", e);
			throw new DataAccessException("Failed to add new recipe", e);
		}
	}
	
	public boolean updateRecipe(int item_id, int ingredient_id, double new_quantity) {
		String sql = "UPDATE Recipes SET quantity_needed = ? WHERE item_id = ? AND ingredient_id = ?";
		
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setDouble(1, new_quantity);
			stmt.setInt(2, item_id);
			stmt.setInt(3, ingredient_id);
			return stmt.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error updating recipe for Item ID: " + item_id, e);
			throw new DataAccessException("Failed to update recipe.", e);
		}
	}
}
