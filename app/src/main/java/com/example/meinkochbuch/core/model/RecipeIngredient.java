package com.example.meinkochbuch.core.model;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Combines an {@link Ingredient} and the necessary amount as well as the {@link Unit} to create an
 * ingredient part in the recipe.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RecipeIngredient {

    Recipe recipe;
    Ingredient ingredient;
    int amount;
    Unit unit;

    @NonNull
    @Override
    public String toString() {
        return "RecipeIngredient{" +
                "recipe='" + recipe.name +"'(ID:"+recipe.id+")"+
                ", ingredient=" + ingredient +
                ", amount=" + amount +
                ", unit=" + unit +
                '}';
    }

    static class SQLRecipeIngredient extends SQLModel<RecipeIngredient> {
        SQLRecipeIngredient(DatabaseHelper database) {
            super(database);
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE RecipeIngredient (",
                    "IngredientID INTEGER,",
                    "RecipeID INTEGER,",
                    "Amount INTEGER,",
                    "Unit TEXT,",
                    "PRIMARY KEY (IngredientID, RecipeID),",
                    "FOREIGN KEY (IngredientID) REFERENCES Ingredient(ID) ON DELETE CASCADE,",
                    "FOREIGN KEY (RecipeID) REFERENCES Recipe(ID) ON DELETE CASCADE", ")"
            );
        }

        @Override
        public void save(RecipeIngredient modelObj) {
            //Ingredient should already be saved beforehand
            DatabaseHelper.DatabaseWriter writer = database.write("RecipeIngredient");
            writer.values.put("IngredientID", modelObj.ingredient.id);
            writer.values.put("RecipeID", modelObj.recipe.id);
            writer.values.put("Amount", modelObj.amount);
            writer.values.put("Unit", modelObj.unit.name());
            writer.close();
        }

        @Override
        public void delete(RecipeIngredient modelObj) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.execSQL("DELETE FROM RecipeIngredient WHERE IngredientID = ? AND RecipeID = ? AND Amount = ? AND Unit = ?",
                    new Object[]{modelObj.ingredient.id, modelObj.recipe.id, modelObj.amount, modelObj.unit.name()});
        }

        @Override
        public Collection<RecipeIngredient> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM RecipeIngredient");
            ArrayList<RecipeIngredient> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long ingredientID = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("IngredientID"));
                    long recipeID = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("RecipeID"));
                    int amount = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("Amount"));
                    String _unit = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Unit"));
                    RecipeIngredient ingredient = new RecipeIngredient();
                    ingredient.amount = amount;
                    ingredient.unit = Unit.valueOf(_unit);
                    ingredient.ingredient = RecipeManager.getInstance().getIngredientByID(ingredientID);
                    ingredient.recipe = RecipeManager.getInstance().getRecipeByID(recipeID);
                    list.add(ingredient);
                } while (reader.cursor.moveToNext());
            }
            reader.close();
            return list;
        }
    }

}
