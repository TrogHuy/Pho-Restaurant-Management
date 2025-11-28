package phorestaurant.controller;

import phorestaurant.dao.MenuItemDAO;
import phorestaurant.dao.RecipeDAO;
import phorestaurant.model.MenuItem;
import phorestaurant.model.Recipe;
import phorestaurant.util.UserSession;
import java.util.List;


public class MenuController {
    private MenuItemDAO menu_dao;
    private RecipeDAO recipe_dao;
    
    public MenuController() {
        this.menu_dao = new MenuItemDAO();
        this.recipe_dao = new RecipeDAO();
    }
    
    public List<MenuItem> getFullMenu() {
    	return menu_dao.getAllMenuItems();
    }
    
    public List<MenuItem> searchMenu(String keyword) {
    	return menu_dao.searchItems(keyword);
    }
    
    public boolean createNewDish(String name, String desc, double price, String cat) {
        if (!UserSession.getInstance().isManager()) {
            System.out.println("Access Denied. Only managers can create new dishes");
            return false;
        }
        
        MenuItem newItem = new MenuItem(name, desc, price, cat);
        return menu_dao.addMenuItem(newItem);
    }
    
    public boolean deleteDish(int id) {
        if (!UserSession.getInstance().isManager()) return false;
        return menu_dao.deleteMenuItem(id);
    }
    
    public boolean addRecipe(int itemId, int ingredientId, double qty) {
        if (!UserSession.getInstance().isManager()) return false;
        Recipe r = new Recipe(itemId, ingredientId, qty);
        return recipe_dao.addRecipe(r);
    }

    
}
