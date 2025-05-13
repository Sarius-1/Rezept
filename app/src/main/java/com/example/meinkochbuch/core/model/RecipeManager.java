package com.example.meinkochbuch.core.model;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.util.Verify;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

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
public class RecipeManager {

    private static final String TAG = "RecipeManager";

    @Getter
    private static RecipeManager instance;

    /**
     * Creates the first instance of this recipe manager which furthermore creates the database.
     * This should be called once on app start.
     * @param context The app context needed for the database.
     */
    public static void init(Context context){
        if(instance != null)return;
        instance = new RecipeManager(context);
        instance.load();
        //instance.test();
    }

    private final Recipe.SQLRecipe sqlRecipe;
    private final Ingredient.SQLIngredient sqlIngredient;
    private final RecipeIngredient.SQLRecipeIngredient sqlRecipeIngredient;
    private final ShoppingListItem.SQLShoppingListItem sqlShoppingListItem;
    private final DatabaseHelper database;
    private RecipeManager(Context context){
        this.database = new DatabaseHelper(context);
        this.sqlRecipe = new Recipe.SQLRecipe(database);
        this.sqlIngredient = new Ingredient.SQLIngredient(database);
        this.sqlRecipeIngredient = new RecipeIngredient.SQLRecipeIngredient(database);
        this.sqlShoppingListItem = new ShoppingListItem.SQLShoppingListItem(database);
    }

    private void test(){
        Log.d(TAG, "Start of test");
        if(RECIPE_BY_ID.isEmpty()){

            Ingredient ingredient = tryRegisterIngredient("Zucker");
            Recipe recipe = createRecipe("Schokokuchen", 60, 6, 5,
                    "This is how you create a cake...");
            addIngredient(recipe, ingredient, 5, Unit.CUBE);
            Log.d(TAG, "Ingredients of "+recipe.name+" are "+recipe.ingredients.size());

        }else{
            Log.d(TAG, "Loading recipe");
            Collection<Recipe> recipes = getRecipesByName("Schokokuchen");
            Log.i(TAG, "Loaded recipes with 'Schokokuchen' = "+recipes);
            for(Recipe recipe : recipes){
                Log.i(TAG, "Ingredients in "+recipe.name+" = "+recipe.ingredients);
            }
            deleteIngredient("Zucker");
            for(Recipe recipe : recipes){
                Log.i(TAG, "Ingredients in "+recipe.name+" = "+recipe.ingredients);
            }
        }

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
            ingredient.recipe.ingredients.add(ingredient);
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

    public void dispose(){
        database.close();
    }

    final Map<String, Ingredient> INGREDIENTS_BY_LOWERCASE_NAME = new HashMap<>();
    final Map<Long, Recipe> RECIPE_BY_ID = new HashMap<>();
    final Map<Long, ShoppingListItem> ITEM_BY_ID = new HashMap<>();

    // -- Ingredients --
    public boolean isIngredientRegistered(String name){
        if(Verify.isInvalidText(name))return false;
        return INGREDIENTS_BY_LOWERCASE_NAME.containsKey(name.trim().toLowerCase());
    }

    public Ingredient getIngredientByName(String name){
        if(Verify.isInvalidText(name))return null;
        return INGREDIENTS_BY_LOWERCASE_NAME.get(name.trim().toLowerCase());
    }

    public Ingredient getIngredientByID(long id){
        for(Ingredient ingredient : INGREDIENTS_BY_LOWERCASE_NAME.values())if(ingredient.id == id)return ingredient;
        return null;
    }

    public Ingredient tryRegisterIngredient(String name){
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

    public void deleteIngredient(Ingredient ingredient){
        if(ingredient == null)return;
        if(!isIngredientRegistered(ingredient.name))return;
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

    public void deleteIngredient(String name){
        deleteIngredient(getIngredientByName(name));
    }

    // -- RecipeIngredient --

    public void addIngredient(@NotNull Recipe recipe, @NotNull Ingredient ingredient, int amount, Unit unit){
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

    public void removeIngredient(Recipe recipe, RecipeIngredient ingredient){
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

    public Recipe getRecipeByID(long id){
        return RECIPE_BY_ID.get(id);
    }

    public Collection<Recipe> getRecipesByName(@NotNull String name, boolean ignoreCase){
        ArrayList<Recipe> list = new ArrayList<>(2);
        for(Recipe recipe : RECIPE_BY_ID.values()){
            if(ignoreCase && name.equalsIgnoreCase(recipe.name))list.add(recipe);
            else if (!ignoreCase && name.equals(recipe.name))list.add(recipe);
        }
        return list;
    }
    public Collection<Recipe> getRecipesByName(@NotNull String name){
        return getRecipesByName(name, false);
    }

    public Recipe createRecipe(@NotNull String name, int processingTime, int portions, int rating, String text){
        Log.i(TAG, "Creating recipe '"+name+"'...");
        Recipe recipe = new Recipe();
        recipe.name = name;
        recipe.processingTime = processingTime;
        recipe.portions = portions;
        recipe.rating = rating;
        recipe.guideText = text;
        sqlRecipe.save(recipe); //this needs to come first!
        RECIPE_BY_ID.put(recipe.id, recipe); //then put into map!
        Log.i(TAG, "Recipe (ID: "+recipe.id+") created!");
        return recipe;
    }

    // -- ShoppingList --

    public Collection<ShoppingListItem> getShoppingList(){
        return ITEM_BY_ID.values();
    }

    public void addShoppingListItem(@NotNull Ingredient ingredient, Unit unit){
        Log.i(TAG, "Adding shopping list item ("+ingredient+", "+unit+")...");
        ShoppingListItem item = new ShoppingListItem();
        item.ingredient = ingredient;
        item.unit = unit;
        sqlShoppingListItem.save(item);
        ITEM_BY_ID.put(item.id, item);
        Log.i(TAG, "Item added! (ID: "+item.id+")");
    }
    public void removeShoppingListItem(@NotNull ShoppingListItem item){
        if(!ITEM_BY_ID.containsKey(item.id))return;
        Log.i(TAG,"Removing shopping list item "+item+"...");
        ITEM_BY_ID.remove(item.id);
        sqlShoppingListItem.delete(item);
        Log.i(TAG, "Item was removed!");
    }
    public void setShoppingListItemChecked(@NotNull ShoppingListItem item, boolean checked){
        if(!ITEM_BY_ID.containsKey(item.id))return;
        if(item.checked == checked)return;
        String state = (checked ? "" : "not ")+"checked";
        Log.i(TAG, "Making item "+item+" "+state+"...");
        sqlShoppingListItem.setChecked(item, checked);
        Log.i(TAG, "Item is now "+state+"!");
    }

}


