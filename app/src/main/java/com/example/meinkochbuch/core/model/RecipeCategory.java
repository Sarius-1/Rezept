package com.example.meinkochbuch.core.model;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class RecipeCategory {

    Recipe recipe;
    Category category;

    @NonNull
    @Override
    public String toString() {
        return "RecipeCategory{" +
                "recipeID=" + recipe.id +
                ", category=" + category +
                '}';
    }

    static class SQLRecipeCategory extends SQLModel<RecipeCategory> {

        public SQLRecipeCategory(DatabaseHelper database) {
            super(database);
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE Category (",
                    "RecipeID INTEGER,",
                    "Name TEXT,",
                    "PRIMARY KEY (RecipeID),",
                    "FOREIGN KEY (RecipeID) REFERENCES Recipe(ID) ON DELETE CASCADE", ")"
            );
        }

        @Override
        public void save(RecipeCategory modelObj) {
            DatabaseHelper.DatabaseWriter writer = database.write("Category");
            writer.values.put("RecipeID", modelObj.recipe.id);
            writer.values.put("Name", modelObj.category.name());
            writer.close();
        }

        @Override
        public void delete(RecipeCategory modelObj) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.execSQL("DELETE FROM Category WHERE RecipeID = ? AND Name = ?",
                    new Object[]{modelObj.recipe.id, modelObj.category.name()});
        }

        @Override
        public Collection<RecipeCategory> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM Category");
            ArrayList<RecipeCategory> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long recipeID = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("RecipeID"));
                    String name = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Name"));
                    RecipeCategory category = new RecipeCategory();
                    category.category = Category.valueOf(name);
                    category.recipe = RecipeManager.getInstance().getRecipeByID(recipeID);
                    list.add(category);
                } while (reader.cursor.moveToNext());
            }
            reader.close();
            return list;
        }
    }

}
