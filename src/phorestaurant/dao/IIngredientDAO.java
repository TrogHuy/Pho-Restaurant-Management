package phorestaurant.dao;

import phorestaurant.model.Ingredients;
import java.util.List;

public interface IIngredientDAO {
	boolean addIngredient(Ingredients ingredient);
	boolean deleteIngredient(int id);
	List<Ingredients> getAllIngredients();
	double getCurrentStock(int ingredient_id);
	boolean updateStock(int id, double changed_quantity);
	boolean isStockLow(int ingredient_id, double threshold);
}
