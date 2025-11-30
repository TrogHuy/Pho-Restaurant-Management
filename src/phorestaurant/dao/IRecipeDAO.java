package phorestaurant.dao;

import phorestaurant.model.Recipe;
import java.util.List;

public interface IRecipeDAO {
	List<Recipe> getRecipeForMenuItem(int item_id);
	boolean addRecipe(Recipe recipe);
	boolean updateRecipe(int item_id, int ingredient_id, double new_quantity);
}
