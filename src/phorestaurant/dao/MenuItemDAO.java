package phorestaurant.dao;

import phorestaurant.model.MenuItem;
import phorestaurant.util.*;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;
import java.util.logging.Level;

public class MenuItemDAO implements IMenuItemDAO {
	private static final Logger LOGGER = Logger.getLogger(MenuItemDAO.class.getName());
	
	@Override
	public List<MenuItem> getAllMenuItems() {
		List<MenuItem> menu = new ArrayList<>();
		String sql = "SELECT * FROM Menu_items";
		try(Connection conn = DatabaseConnection.getConnection();
				Statement stmt = conn.createStatement();
				ResultSet rs = stmt.executeQuery(sql)) {
			
			while(rs.next()) {
				MenuItem item = new MenuItem(
					rs.getInt("item_id"),
					rs.getString("name"),
					rs.getString("description"),
					rs.getDouble("price"),
					rs.getString("category")
				);
				menu.add(item);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to fetch all menu items.", e);
			throw new DataAccessException("Could not retrieve menu items from the database.", e);
		}
		return menu;
	}
	
	@Override
	public MenuItem getItemById(int id) {
		String sql = "SELECT * FROM Menu_items WHERE item_id = ?";
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, id);
			ResultSet rs = stmt.executeQuery();
			
			if(rs.next()) {
				return new MenuItem(
					rs.getInt("item_id"),
					rs.getString("name"),
					rs.getString("description"),
					rs.getDouble("price"),
					rs.getString("category")
				);
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to search for menu item using id");
			throw new DataAccessException("Could not retrieve specific menu item from the database.", e);
		}
		return null;
	}
	
	@Override
	public List<MenuItem> searchItems(String keyword) {
		List<MenuItem> menu = new ArrayList<>();
		String sql = "SELECT * FROM Menu_items WHERE name LIKE ?";
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, "%" + keyword + "%") ;
			ResultSet rs = stmt.executeQuery();
			
			while(rs.next()) {
				menu.add(new MenuItem(
						rs.getInt("item_id"),
						rs.getString("name"),
						rs.getString("description"),
						rs.getDouble("price"),
						rs.getString("category")
				));
			}
		} catch (SQLException e) {
			LOGGER.log(Level.SEVERE, "Failed to search for menu items using keyword", e);
			throw new DataAccessException("Failed to search for items with keyword: ." + keyword, e);
		}
		return menu;
	}
	
	@Override
	public boolean addMenuItem(MenuItem item) {
		String sql = "INSERT INTO Menu_items (name, description, price, category) VALUES (?,?,?,?)";
		try(Connection conn = DatabaseConnection.getConnection();
				PreparedStatement stmt = conn.prepareStatement(sql)) {
			stmt.setString(1, item.getName());
			stmt.setString(2, item.getName());
			stmt.setDouble(3, item.getPrice());
			stmt.setString(4, item.getCategory());
			return stmt.executeUpdate() > 0;
		} catch(SQLException e) {
			LOGGER.log(Level.SEVERE, "Error adding menu item: " + item.getName(), e);
			throw new DataAccessException("Failed to add new menu item.", e);
		}
	}
	
	@Override
	public boolean deleteMenuItem(int id) {
		String delete_recipe_sql = "DELETE FROM Recipes where item_id = ?";
		String delete_menu_item = "DELETE FROM Menu_items where item_id = ?";
		
		Connection conn = null;
		try {
			conn = DatabaseConnection.getConnection();
			conn.setAutoCommit(false);
			
			try (PreparedStatement del_recipe_stmt = conn.prepareStatement(delete_recipe_sql)) {
				del_recipe_stmt.setInt(1, id);
				del_recipe_stmt.executeUpdate();
			}
			
			int rows = 0;
			try (PreparedStatement del_menuitem_stmt = conn.prepareStatement(delete_menu_item)) {
				del_menuitem_stmt.setInt(1, id);
				rows = del_menuitem_stmt.executeUpdate();
			}
			conn.commit();
			return rows > 0;
		}
		catch(SQLException e) {
			if(conn != null) {
				try {conn.rollback();} 
				catch(SQLException ex) {ex.printStackTrace();}
			}
			LOGGER.log(Level.SEVERE, "Error deleting menu item id: " + id, e);
			throw new DataAccessException("Failed to delete item. It may be part of existing orders.", e);
		} finally {
			if(conn != null) try {conn.setAutoCommit(true); conn.close();}
			catch(SQLException e) {e.printStackTrace();}
		}
	}
}
