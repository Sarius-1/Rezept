package com.example.meinkochbuch.core.model;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * The recipe is the main model which contains all necessary attributes to provide graphical information.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Recipe {

    // -- Primary key --
    long id;

    // -- Content --
    String name;
    int processingTime, portions, rating;
    String guideText;
    List<RecipeIngredient> ingredients = new ArrayList<>();
    Set<RecipeCategory> categories = new HashSet<>();

    public RecipeIngredient getContainingIngredient(Ingredient ingredient){
        for(RecipeIngredient in : ingredients)if(in.ingredient.equals(ingredient))return in;
        return null;
    }

    public boolean containsIngredients(Ingredient... ingredients){
        return containsIngredients(Arrays.asList(ingredients));
    }

    public boolean containsIngredients(Collection<Ingredient> ingredients){
        int count = 0;
        for(Ingredient criteria : ingredients){
            if(criteria == null){
                count++;
                continue;
            }
            for(RecipeIngredient ingredient : this.ingredients)if(ingredient.ingredient.equals(criteria))count++;
        }
        return count == ingredients.size();
    }

    public boolean isCategorizedAs(Category... categories){
        if(categories == null || categories.length == 0)return false;
        int count = 0;
        for(Category category : categories){
            if(category == null){
                count++;
                continue;
            }
            for(RecipeCategory rc : this.categories)if(rc.category.equals(category))count++;
        }
        return count == categories.length;
    }

    @NonNull
    @Override
    public String toString() {
        return "Recipe{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", processingTime=" + processingTime +
                ", portions=" + portions +
                ", rating=" + rating +
                ", guideText='" + guideText + '\'' +
                ", ingredients=" + ingredients +
                '}';
    }



    static class SQLRecipe extends SQLModel<Recipe>{

        SQLRecipe(DatabaseHelper database) {
            super(database);
        }

        @Override
        protected String tableName() {
            return "Recipe";
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE "+tableName+" (",
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "Name TEXT NOT NULL,",
                    "ProcessingTime INTEGER,",
                    "Portions INTEGER,",
                    "Description INTEGER,",
                    "Rating INTEGER)");
        }

        @Override
        public void save(Recipe recipe) {
            DatabaseHelper.DatabaseWriter writer = database.write(tableName);
            writer.values.put("Name", recipe.name);
            writer.values.put("ProcessingTime", recipe.processingTime);
            writer.values.put("Portions", recipe.portions);
            writer.values.put("Rating", recipe.rating);
            writer.values.put("Description", recipe.guideText);
            recipe.id = writer.closeGetID();
        }

        @Override
        public void delete(Recipe modelObj) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.execSQL("DELETE FROM "+tableName+" WHERE ID = ?", new Object[]{modelObj.id});
        }

        @Override
        public Collection<Recipe> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM "+tableName+" ORDER BY ID ASC");
            ArrayList<Recipe> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long id = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("ID"));
                    String name = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Name"));
                    int processingTime = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("ProcessingTime"));
                    int portions = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("Portions"));
                    int rating = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("Rating"));
                    String description = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Description"));
                    Recipe recipe = new Recipe();
                    recipe.id = id;
                    recipe.name = name;
                    recipe.processingTime = processingTime;
                    recipe.portions = portions;
                    recipe.rating = rating;
                    recipe.guideText = description;
                    list.add(recipe);
                } while (reader.cursor.moveToNext());
            }
            reader.close();
            return list;
        }

    }

}
