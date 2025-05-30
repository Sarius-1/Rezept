package com.example.meinkochbuch.core.model;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import com.example.meinkochbuch.filter.FilterCriteria;
import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.ImageDatabase;
import com.example.meinkochbuch.util.Verify;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import lombok.Getter;

/**
 * Main class for the hole recipe-model. It manages the SQLite-Database connection behind objects like:
 * <l>
 *     <li>{@link Recipe}</li>
 *     <li>{@link Ingredient}</li>
 *     <li>{@link RecipeIngredient}</li>
 *     <li>{@link ShoppingListItem}</li>
 *     <li>(as well as {@link Unit})</li>
 * </l>
 * and holds the instance of the {@link DatabaseHelper}.
 * <p>
 * All use cases regarding recipe, ingredients, etc. are managed inside this class and shouldn't be manually changed outside.
 * </p>
 */
public final class RecipeManager {

    private static final String TAG = "RecipeManager";

    @Getter
    private static RecipeManager instance;

    /**
     * Creates the first instance of this recipe manager which furthermore creates the database.
     * This should be called once on app start.
     * @param context The app context needed for the database.
     */
    public static void init(@NotNull Context context){
        if(instance != null)return;
        instance = new RecipeManager(context);
        instance.load();
        //instance.test();
    }

    private final Recipe.SQLRecipe sqlRecipe;
    private final Ingredient.SQLIngredient sqlIngredient;
    private final RecipeIngredient.SQLRecipeIngredient sqlRecipeIngredient;
    private final RecipeCategory.SQLRecipeCategory sqlRecipeCategory;
    private final ShoppingListItem.SQLShoppingListItem sqlShoppingListItem;
    private final DatabaseHelper database;
    private RecipeManager(Context context){
        this.database = new DatabaseHelper(context);
        this.sqlRecipe = new Recipe.SQLRecipe(database);
        this.sqlIngredient = new Ingredient.SQLIngredient(database);
        this.sqlRecipeIngredient = new RecipeIngredient.SQLRecipeIngredient(database);
        this.sqlRecipeCategory = new RecipeCategory.SQLRecipeCategory(database);
        this.sqlShoppingListItem = new ShoppingListItem.SQLShoppingListItem(database);
    }

    /**
     * Loads all model objects from the database into memory.
     */
    private void load(){
        Log.i(TAG, "Loading everything from database...");
        Collection<Ingredient> ingredients = sqlIngredient.loadAll();
        for(Ingredient ingredient : ingredients)
            INGREDIENTS_BY_LOWERCASE_NAME.put(ingredient.name.trim().toLowerCase(), ingredient);

        Collection<Recipe> recipes = sqlRecipe.loadAll();
        for(Recipe recipe : recipes){
            Log.d(TAG, "Loading recipe "+recipe.name);
            RECIPE_BY_ID.put(recipe.id, recipe);
        }

        Collection<RecipeIngredient> recipeIngredients = sqlRecipeIngredient.loadAll();
        for(RecipeIngredient ingredient : recipeIngredients){
            ingredient.recipe.ingredients.add(ingredient); //add self to recipe
        }

        Collection<RecipeCategory> recipeCategories = sqlRecipeCategory.loadAll();
        for(RecipeCategory category : recipeCategories){
            category.recipe.categories.add(category); //add self to recipe
        }

        Collection<ShoppingListItem> items = sqlShoppingListItem.loadAll();
        for(ShoppingListItem item : items){
            ITEM_BY_ID.put(item.id, item);
        }

        Log.i(TAG, "Content loaded:");
        Log.i(TAG, "Recipes: "+RECIPE_BY_ID.size());
        Log.i(TAG, "Ingredients: "+INGREDIENTS_BY_LOWERCASE_NAME.size());
        Log.i(TAG, "ShoppingListItems: "+ITEM_BY_ID.size());
    }

    /**
     * Disposes this manager and closes the database.
     */
    public void dispose(){
        database.close();
        instance = null;
    }

    final Map<String, Ingredient> INGREDIENTS_BY_LOWERCASE_NAME = new HashMap<>();
    final Map<Long, Recipe> RECIPE_BY_ID = new HashMap<>();
    final Map<Long, ShoppingListItem> ITEM_BY_ID = new HashMap<>();

    // -- Ingredients --

    /**
     * Checks whether an ingredients name is already registered.
     * @param name The name to check for.
     * @return {@code true} if it is already registered, {@link false} otherwise.
     */
    public boolean isIngredientRegistered(@NotNull String name){
        if(Verify.isInvalidText(name))return false;
        return INGREDIENTS_BY_LOWERCASE_NAME.containsKey(name.trim().toLowerCase());
    }

    /**
     * Tries to get an ingredient by a specified name. The ingredient must be loaded/registered beforehand.
     * Otherwise it will return {@code null}.
     * @param name The name of the ingredient.
     * @return The ingredient registered by the name or {@code null} if there is non.
     */
    public Ingredient getIngredientByName(@NotNull String name){
        if(Verify.isInvalidText(name))return null;
        return INGREDIENTS_BY_LOWERCASE_NAME.get(name.trim().toLowerCase());
    }

    /**
     * Tries to get an ingredient by a specified id.
     * The ingredient must be loaded/registered beforehand, otherwise it will return {@code null}.
     * @param id The id of the ingredient.
     * @return The ingredient registered with that id or {@code null} if there is non.
     */
    public Ingredient getIngredientByID(long id){
        for(Ingredient ingredient : INGREDIENTS_BY_LOWERCASE_NAME.values())if(ingredient.id == id)return ingredient;
        return null;
    }

    /**
     * Tries to register an ingredient with a specified name. If an ingredient with that name is already registered
     * (ignoring prefix/suffix whitespaces and case sensitivity), the already registered one will be returned.
     * Otherwise a new object will be created and saved into the database.
     * If the name is invalid just {@code null} will be returned.
     * <p>For more information see {@link Verify#isInvalidText(String)}.</p>
     * @param name The name of the ingredient to register.
     * @return The newly registered ingredient (if it isn't already, otherwise the registered one is returned) or
     * {@code null} if the name is invalid.
     */
    public Ingredient tryRegisterIngredient(@NotNull String name){
        if(Verify.isInvalidText(name))return null;
        String search = name.trim().toLowerCase();
        Ingredient ingredient = INGREDIENTS_BY_LOWERCASE_NAME.getOrDefault(search, null);
        if(ingredient != null)return ingredient;
        Log.i(TAG, "Registering ingredient '"+name+"'...");
        ingredient = new Ingredient();
        ingredient.name = name;
        sqlIngredient.save(ingredient);
        INGREDIENTS_BY_LOWERCASE_NAME.put(search, ingredient);
        Log.i(TAG, "Ingredient registered! (ID: "+ingredient.id+")");
        return ingredient;
    }

    /**
     * Deletes an ingredient from the database and local memory.
     * This will result in all recipes will have all {@link RecipeIngredient} removed that hold the specified ingredient,
     * as well as all {@link ShoppingListItem} that also hold that ingredient.
     * @param ingredient The ingredient to delete.
     */
    public void deleteIngredient(Ingredient ingredient){
        if(ingredient == null){
            Log.e(TAG, "Tried to delete ingredient but it is null!");
            return;
        }
        if(!isIngredientRegistered(ingredient.name)){
            Log.e(TAG, "Tried to delete ingredient "+ingredient+" but it isn't registered!");
            return;
        }
        Log.i(TAG, "Deleting ingredient '"+ingredient.name+"' (ID: "+ingredient.id+")...");
        sqlIngredient.delete(ingredient);
        INGREDIENTS_BY_LOWERCASE_NAME.remove(ingredient.name.trim().toLowerCase());
        //remove from recipe
        for(Recipe recipe : RECIPE_BY_ID.values()){
            removeIngredient(recipe, recipe.getContainingIngredient(ingredient));
        }
        //remove from shopping list
        ArrayList<ShoppingListItem> loop = new ArrayList<>(getShoppingList());
        for(ShoppingListItem item : loop){
            if(item.ingredient.equals(ingredient))removeShoppingListItem(item);
        }
        Log.i(TAG, "Ingredient (ID: "+ingredient.id+") deleted!");
    }

    /**
     * Deletes and ingredient by name.
     * For more information see {@link #deleteIngredient(Ingredient)}.
     * @param name The name of the ingredient to delete.
     */
    public void deleteIngredient(@NotNull String name){
        deleteIngredient(getIngredientByName(name));
    }

    // -- RecipeIngredient --

    /**
     * Adds an ingredient to a given recipe.
     * @param recipe The recipe to add an ingredient to.
     * @param ingredient The ingredient to add.
     * @param amount The amount used for displaying in combination with the unit.
     * @param unit The unit used for displaying in combiantion with the amount.
     */
    public void addIngredient(@NotNull Recipe recipe, @NotNull Ingredient ingredient, double amount, Unit unit){
        if(getRecipeByID(recipe.id) == null){
            Log.e(TAG, "Tried to add an ingredient to "+recipe+" but the recipe isn't registered!");
            return; //recipe is not registered
        }
        if(getIngredientByName(ingredient.name) == null){
            Log.e(TAG, "Tried to add ingredient "+ingredient+" to "+recipe+" but the ingredient isn't registered!");
            return;
        }
        Log.i(TAG, "Adding ingredient '"+ingredient.name+"'...");
        RecipeIngredient ri = new RecipeIngredient();
        ri.recipe = recipe;
        ri.ingredient = ingredient;
        ri.amount = amount;
        ri.unit = unit;
        recipe.ingredients.add(ri);
        sqlRecipeIngredient.save(ri);
        Log.i(TAG, "Registered recipe ingredient '"+ingredient.name+"' "+amount+" "+unit);
    }

    /**
     * Removes an ingredient from a given recipe.
     * @param recipe The recipe to remove from.
     * @param ingredient The ingredient to remove.
     */
    public void removeIngredient(@NotNull Recipe recipe, @NotNull RecipeIngredient ingredient){
        if(getRecipeByID(recipe.id) == null){
            Log.e(TAG, "Tried to remove ingredient from recipe "+recipe+" but the recipe isn't registered!");
            return;
        }
        if(!recipe.ingredients.contains(ingredient) || !ingredient.recipe.equals(recipe)){
            Log.w(TAG, "Tried to remove ingredient from recipe "+recipe+" but it doesn't have that ingredient!");
            return;
        }
        Log.i(TAG, "Removing ingredient "+ingredient.ingredient.name+" (ID: "+ingredient.ingredient.id+" from recipe '"+
                recipe.name+"' (ID: "+recipe.id+")...");
        LinkedList<RecipeIngredient> toRemove = new LinkedList<>();
        for(RecipeIngredient ri : recipe.getIngredients()){
            if(ri.ingredient.equals(ingredient.ingredient))toRemove.add(ri);
        }
        for(RecipeIngredient rem : toRemove){
            recipe.ingredients.remove(rem);
            sqlRecipeIngredient.delete(ingredient);
        }
        Log.i(TAG, "Ingredient removed!");
    }

    // -- Recipe --

    /**
     * Retrieves a recipe by a given id.
     * @param id The id to receive the recipe for.
     * @return The registered recipe or {@code null} if there is non for this id.
     */
    public Recipe getRecipeByID(long id){
        return RECIPE_BY_ID.get(id);
    }

    /**
     * Collects all recipes by a given name and returns the created set.
     * @param name The name of the recipes to get.
     * @param ignoreCase Whether to ignore case sensitivity.
     * @return A collection of all recipes that have the equal name (or similar if {@code ignoreCase} is {@code true}).
     * @deprecated This will shouldn't be used and might get removed soon. Use {@link #filter(FilterCriteria...)} 
     * with {@link FilterCriteria#nameEquals(String)}.
     */
    @Deprecated()
    public Collection<Recipe> getRecipesByName(@NotNull String name, boolean ignoreCase){
        if(RECIPE_BY_ID.isEmpty())return Collections.emptyList();
        ArrayList<Recipe> list = new ArrayList<>(2);
        for(Recipe recipe : RECIPE_BY_ID.values()){
            if(ignoreCase && name.equalsIgnoreCase(recipe.name))list.add(recipe);
            else if (!ignoreCase && name.equals(recipe.name))list.add(recipe);
        }
        return list;
    }
    @Deprecated
    public Collection<Recipe> getRecipesByName(@NotNull String name){
        return getRecipesByName(name, false);
    }

    /**
     * Creates a new recipe with a given name and following parameters. This will then registered in the database and app cache
     * and finally returned.
     * @param name The name of the recipe.
     * @param processingTime The processing time.
     * @param portions The portions.
     * @param text The description (can be {@code null}).
     * @return The newly created recipe.
     */
    public Recipe createRecipe(@NotNull String name, int processingTime, int portions, String text){
        Log.i(TAG, "Creating recipe '"+name+"'...");
        Recipe recipe = new Recipe();
        recipe.name = name;
        recipe.processingTime = processingTime;
        recipe.portions = portions;
        recipe.guideText = text;
        sqlRecipe.save(recipe); //this needs to come first!
        RECIPE_BY_ID.put(recipe.id, recipe); //then put into map!
        Log.i(TAG, "Recipe (ID: "+recipe.id+") created!");
        return recipe;
    }

    /**
     * Deletes a recipe. The recipe must be registered.
     * @param recipe The recipe to delete.
     * @return Whether the deletion was successful.
     */
    public boolean deleteRecipe(@NotNull Recipe recipe){
        if(!RECIPE_BY_ID.containsKey(recipe.id)){
            Log.w(TAG, "Tried to remove recipe "+recipe+" but it is not registered!");
            return false;
        }
        Log.i(TAG, "Deleting recipe (ID:"+recipe.id+")!");
        for(RecipeIngredient ingredient : recipe.ingredients)sqlRecipeIngredient.delete(ingredient);
        sqlRecipe.delete(recipe);
        RECIPE_BY_ID.remove(recipe.id);
        Log.i(TAG, "Recipe deleted!");
        return true;
    }

    /**
     * Deletes a recipe by id.
     * @param id The id of the recipe.
     * @return Whether the deletion was successful.
     */
    public boolean deleteRecipe(int id){
        Recipe recipe = getRecipeByID(id);
        if(recipe == null)return false;
        return deleteRecipe(recipe);
    }

    /**
     * Sets the rating of a recipe to the specified one (must be in range of 0-5, both inclusive).
     * @param recipe The recipe to set the rating for.
     * @param rating The rating to set.
     */
    public void setRating(@NotNull Recipe recipe, int rating){
        if(rating < 0 || rating > 5){
            Log.e(TAG, "Cannot apply rating to recipe (ID:"+recipe.id+") because '"+rating+"' isn't in range (0-5).");
            return;
        }
        if(!RECIPE_BY_ID.containsKey(recipe.id)){
            Log.e(TAG, "Tried to set rating of recipe "+recipe+" but it is not registered!");
            return;
        }
        Log.i(TAG, "Setting rating to "+rating+" of recipe (ID:"+recipe.id+")...");
        sqlRecipe.setRating(recipe, rating);
        recipe.rating = rating;
        Log.i(TAG, "Rating successfully set!");
    }

    //Images

    public boolean setImage(@NotNull Recipe recipe, @NotNull Uri imageUri){
        if(!RECIPE_BY_ID.containsKey(recipe.id)){
            Log.e(TAG, "Tried to add image to recipe (ID:"+recipe.id+") but it isn't registered!");
            return false;
        }
        return ImageDatabase.getInstance().saveFile(imageUri, "recipe"+recipe.id);
    }

    // -- Categories --

    /**
     * Adds a category to a recipe. This will do nothing if it was already added.
     * @param recipe The recipe to add the given category to.
     * @param category The given category to add.
     */
    public void addCategory(@NotNull Recipe recipe, @NotNull Category category){
        if(recipe.isCategorizedAs(category))return;
        Log.i(TAG, "Adding category "+category+" to Recipe (ID:"+recipe.id+")...");
        RecipeCategory rc = new RecipeCategory();
        rc.recipe = recipe;
        rc.category = category;
        sqlRecipeCategory.save(rc);
        recipe.categories.add(rc);
        Log.i(TAG, "Added category to the recipe!");
    }

    /**
     * Removes a category from a recipe. This will do nothing if it doesn't have the specified category.
     * @param recipe The recipe to remove the given category from.
     * @param category The given category to remove.
     */
    public void removeCategory(@NotNull Recipe recipe, @NotNull Category category){
        if(!recipe.isCategorizedAs(category))return;
        Log.i(TAG, "Removing category "+category+" from Recipe (ID:"+recipe.id+")...");
        Iterator<RecipeCategory> itr = recipe.categories.iterator();
        while (itr.hasNext()){
            RecipeCategory rc = itr.next();
            if(rc.category != category)continue;
            sqlRecipeCategory.delete(rc);
            itr.remove();
            Log.i(TAG, "Removed category from the recipe!");
            break;
        }
    }

    // -- ShoppingList --

    /**
     * Retrieves all items stored in the local cache of the shopping list.
     * <p><b>Note:</b> this is a copy of the internal cache!</p>
     * @return The list of items representing the shopping list.
     */
    public Collection<ShoppingListItem> getShoppingList(){
        return new ArrayList<>(ITEM_BY_ID.values());
    }

    /**
     * Adds an item to the shopping list.
     * @param ingredient The ingredient to add.
     * @param unit The unit for the ingredient.
     */
    public ShoppingListItem addShoppingListItem(@NotNull Ingredient ingredient, double amount, Unit unit){
        Log.i(TAG, "Adding shopping list item ("+ingredient+", "+unit+")...");
        for(ShoppingListItem item : getShoppingList()){
            if(item.ingredient == ingredient && item.unit == unit){
                //if item already exists, just set its amount.
                setShoppingListItemAmount(item, amount);
                return item;
            }
        }
        ShoppingListItem item = new ShoppingListItem();
        item.ingredient = ingredient;
        item.amount = amount;
        item.unit = unit;
        sqlShoppingListItem.save(item);
        ITEM_BY_ID.put(item.id, item);
        Log.i(TAG, "Item added! (ID: "+item.id+")");
        return item;
    }

    /**
     * Removes an item from the shopping list.
     * @param item The item to remove.
     */
    public void removeShoppingListItem(@NotNull ShoppingListItem item){
        if(!ITEM_BY_ID.containsKey(item.id)){
            Log.e(TAG, "Tried to remove shopping list item "+item+" but it isn't registered.");
            return;
        }
        Log.i(TAG,"Removing shopping list item "+item+"...");
        ITEM_BY_ID.remove(item.id);
        sqlShoppingListItem.delete(item);
        Log.i(TAG, "Item was removed!");
    }

    /**
     * Sets an item in the shopping list to either checked or not.
     * @param item The item to set the checked-state for.
     * @param checked Whether it is now checked or not.
     */
    public void setShoppingListItemChecked(@NotNull ShoppingListItem item, boolean checked){
        if(!ITEM_BY_ID.containsKey(item.id)){
            Log.e(TAG, "Tried to change the checked status for "+item+" but it isn't registered!");
            return;
        }
        if(item.checked == checked)return;
        String state = (checked ? "" : "not ")+"checked";
        Log.i(TAG, "Making item "+item+" "+state+"...");
        sqlShoppingListItem.setChecked(item, checked);
        item.checked = checked;
        Log.i(TAG, "Item is now "+state+"!");
    }

    /**
     * Sets the amount of a specified shopping list item.
     * @param item The item to set the amount for.
     * @param amount The amount to set.
     */
    public void setShoppingListItemAmount(@NotNull ShoppingListItem item, double amount){
        if(amount <= 0){
            if(ITEM_BY_ID.containsKey(item.id))removeShoppingListItem(item);
            return;
        }
        if(item.amount == amount)return;
        Log.i(TAG, "Setting item (ID:"+item.id+") amount to "+amount+"...");
        sqlShoppingListItem.setAmount(item, amount);
        item.amount = amount;
        Log.i(TAG, "Item (ID:"+item.id+") amount was changed to "+amount+"!");
    }

    // -- Filter --

    /**
     * Function that filters all registered recipes by given filter criteria. For more information see {@link FilterCriteria}.
     * @param filters The filters to apply.
     * @return The collected recipes fitting the filters.
     */
    public Set<Recipe> filter(FilterCriteria... filters){
        if(filters == null || filters.length == 0)return Collections.emptySet();
        HashSet<Recipe> set = new HashSet<>();
        for(Recipe recipe : RECIPE_BY_ID.values()){
            for(FilterCriteria criteria : filters){
                if(criteria == null)continue;
                if(criteria.accept(recipe))set.add(recipe);
            }
        }
        return set;
    }

    public Collection<Ingredient> getAllIngredients() {
        return INGREDIENTS_BY_LOWERCASE_NAME.values();
    }
}


