package com.example.meinkochbuch;

import android.content.Context;

import androidx.test.platform.app.InstrumentationRegistry;
import androidx.test.ext.junit.runners.AndroidJUnit4;

import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.*;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;
import com.example.meinkochbuch.core.model.RecipeManager;
import com.example.meinkochbuch.core.model.ShoppingListItem;
import com.example.meinkochbuch.core.model.Unit;
import com.example.meinkochbuch.filter.FilterCriteria;

import java.util.Collection;
import java.util.Set;
import java.util.function.Predicate;

/**
 * Instrumented test, which will execute on an Android device.
 *
 * @see <a href="http://d.android.com/tools/testing">Testing documentation</a>
 */
@RunWith(AndroidJUnit4.class)
public class ExampleInstrumentedTest {
    @Test
    public void useAppContext() {
        // Context of the app under test.
        Context appContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        RecipeManager.init(appContext);

        // -- Ingredients --
        Ingredient sugar = RecipeManager.getInstance().tryRegisterIngredient("Zucker");
        Ingredient flour = RecipeManager.getInstance().tryRegisterIngredient("Mehl");
        Ingredient salt = RecipeManager.getInstance().tryRegisterIngredient("Salz");
        assert RecipeManager.getInstance().isIngredientRegistered("Zucker");
        assert RecipeManager.getInstance().isIngredientRegistered("MEHL");
        assert RecipeManager.getInstance().isIngredientRegistered(" salz ");

        assertEquals(RecipeManager.getInstance().getIngredientByName("Mehl"), flour);
        assertEquals(RecipeManager.getInstance().getIngredientByID(salt.getId()), salt);

        RecipeManager.getInstance().deleteIngredient("Salz");
        assert RecipeManager.getInstance().getRecipesByName("Salz").isEmpty();

        // -- Recipe --
        Recipe recipe = RecipeManager.getInstance().createRecipe("Kuchen", 180,1,"Some pie");
        assert !RecipeManager.getInstance().getRecipesByName("Kuchen").isEmpty();

        RecipeManager.getInstance().addIngredient(recipe, sugar, 50, Unit.FOOTBALL_FIELD);
        RecipeManager.getInstance().addIngredient(recipe, flour, 250, Unit.GRAM);
        assert recipe.containsIngredients(sugar);
        assert recipe.containsIngredients(flour);

        RecipeManager.getInstance().removeIngredient(recipe, recipe.getIngredients().get(0));
        assert !recipe.containsIngredients(sugar);

        RecipeManager.getInstance().setRating(recipe, 5);
        assert recipe.getRating() == 5;

        RecipeManager.getInstance().deleteRecipe(recipe);
        assert RecipeManager.getInstance().getRecipesByName(recipe.getName()).isEmpty();

        // -- Category & Filter --
        recipe = RecipeManager.getInstance().createRecipe("Torte", 60, 3,  "Some cake");
        RecipeManager.getInstance().addIngredient(recipe, flour, 150, Unit.GRAM);
        RecipeManager.getInstance().addCategory(recipe, Category.VEGETARIAN);
        RecipeManager.getInstance().addCategory(recipe, Category.GLUTEN_FREE);
        assert recipe.isCategorizedAs(Category.VEGETARIAN, Category.GLUTEN_FREE);

        RecipeManager.getInstance().addCategory(recipe, Category.LACTOSE_FREE);
        Set<Recipe> filter = RecipeManager.getInstance().filter(FilterCriteria.categorizes(Category.VEGETARIAN, Category.LACTOSE_FREE),
                FilterCriteria.equalsTime(60));
        assert filter.contains(recipe);

        Set<Recipe> wrongFilter = RecipeManager.getInstance().filter(FilterCriteria.equalsRating(5));
        assert !wrongFilter.contains(recipe);

        RecipeManager.getInstance().removeCategory(recipe, Category.VEGETARIAN);
        assert !recipe.isCategorizedAs(Category.VEGETARIAN);

        // -- ShoppingList --
        RecipeManager.getInstance().addShoppingListItem(sugar, 5, Unit.CUBE);
        Collection<ShoppingListItem> items = RecipeManager.getInstance().getShoppingList();
        assert items.stream().anyMatch(item -> item.getIngredient() == sugar);

        RecipeManager.getInstance().setShoppingListItemChecked(items.stream().findFirst().get(), true);
        assert items.stream().findFirst().get().isChecked();

        RecipeManager.getInstance().setShoppingListItemAmount(items.stream().findFirst().get(), 20);
        assert items.stream().findFirst().get().getAmount() == 20;

        RecipeManager.getInstance().removeShoppingListItem(items.stream().findFirst().get());
        assert RecipeManager.getInstance().getShoppingList().isEmpty();

    }

}