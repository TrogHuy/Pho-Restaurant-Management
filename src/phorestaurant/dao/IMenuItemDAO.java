package phorestaurant.dao;

import phorestaurant.model.MenuItem;
import java.util.List;

public interface IMenuItemDAO {
	boolean addMenuItem(MenuItem item);
	boolean deleteMenuItem(int id);
	
	List<MenuItem> getAllMenuItems();
	MenuItem getItemById(int id);
	List<MenuItem> searchItems(String keyword);
}
