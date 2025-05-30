package com.example.meinkochbuch.filter;

import com.example.meinkochbuch.core.model.Category;
import com.example.meinkochbuch.core.model.Ingredient;
import com.example.meinkochbuch.core.model.Recipe;

/**
 * Functional interface used to search for one criteria in a {@link Recipe}.
 * Default implementations are documented below.
 */
public interface FilterCriteria {

    /**
     * Builds a filter criteria which checks if a recipes name contains the given text part.
     * @param part The part checked against the recipes name.
     * @return The build filter.
     */
    static FilterCriteria nameContains(String part){
        return recipe -> recipe.getName().toLowerCase().contains(part.toLowerCase());
    }

    /**
     * Builds a filter criteria which checks if a recipes name equals the given text part.
     * @param name The name checked against the recipes name.
     * @param ignoreCase Whether to ignore case sensitivity.
     * @return The build filter.
     */
    static FilterCriteria nameEquals(String name, boolean ignoreCase){
        return recipe -> ignoreCase ? recipe.getName().equalsIgnoreCase(name) : recipe.getName().equals(name);
    }
    static FilterCriteria nameEquals(String name){
        return nameEquals(name, true);
    }

    /**
     * Builds a filter criteria which checks if a recipe includes all given ingredients.
     * @param ingredients The ingredients which must be included in the recipe.
     * @return The build filter.
     */
    static FilterCriteria containsIngredients(Ingredient... ingredients){
        return recipe -> recipe.containsIngredients(ingredients);
    }

    /**
     * Builds a filter criteria which checks if a recipes rating is equal to a given one.
     * @param rating The rating to check for.
     * @return The build filter.
     */
    static FilterCriteria equalsRating(int rating){
        return recipe -> recipe.getRating() == rating;
    }

    /**
     * Builds a filter criteria which checks if a recipes rating is in the range of two min and max bounds (both inclusive).
     * @param min The min rating required (inclusive).
     * @param max The max rating required (inclusive).
     * @return The build filter.
     */
    static FilterCriteria minMaxRating(int min, int max){
        return recipe -> recipe.getRating() >= min && recipe.getRating() <= max;
    }

    /**
     * Builds a filter criteria which checks if a recipes processing time is equal to a given one.
     * @param time The time to check for.
     * @return The build filter.
     */
    static FilterCriteria equalsTime(int time){
        return recipe -> recipe.getProcessingTime() == time;
    }

    /**
     * Builds a filter criteria which checks if a recipes processing time is in the range of two min and max bounds (both inclusive).
     * @param min The min processing time required (inclusive).
     * @param max The max processing time required (inclusive).
     * @return The build filter.
     */
    static FilterCriteria minMaxTime(int min, int max){
        return recipe -> recipe.getProcessingTime() >= min && recipe.getProcessingTime() <= max;
    }

    static FilterCriteria any(){
        return recipe -> true;
    }

    static FilterCriteria categorizes(Category... categories){
        return recipe -> recipe.isCategorizedAs(categories);
    }

    /**
     * Called to test the current filter criteria implementation against a given recipe.
     * @param recipe The recipe to test against.
     * @return {@code true} if the criteria matches the recipe, {@code false} otherwise.
     */
    boolean accept(Recipe recipe);

}
