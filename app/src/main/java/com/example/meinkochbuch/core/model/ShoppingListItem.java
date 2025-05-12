package com.example.meinkochbuch.core.model;

import android.content.ContentValues;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Collection;

import lombok.Getter;

@Getter
public class ShoppingListItem {

    long id;
    Ingredient ingredient;
    Unit unit;
    boolean checked;

    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "id=" + id +
                ", ingredient=" + ingredient +
                ", unit=" + unit +
                ", checked=" + checked +
                '}';
    }

    public static class SQLShoppingListItem extends SQLModel<ShoppingListItem> {
        public SQLShoppingListItem(DatabaseHelper database) {
            super(database);
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE ShoppingList (",
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "IngredientID INTEGER,",
                    "Unit TEXT,",
                    "Checked INTEGER DEFAULT 0,",
                    "FOREIGN KEY (IngredientID) REFERENCES Ingredient(ID)", ")"
            );
        }

        @Override
        public void save(ShoppingListItem modelObj) {
            DatabaseHelper.DatabaseWriter writer = database.write("ShoppingList");
            writer.values.put("IngredientID", modelObj.ingredient.id);
            writer.values.put("Unit", modelObj.unit.name());
            writer.values.put("Checked", modelObj.checked ? 1 : 0);
            writer.close();
        }

        @Override
        public void delete(ShoppingListItem modelObj) {
            database.getWritableDatabase().execSQL(
                    "DELETE FROM ShoppingList WHERE ID = ?", new Object[]{String.valueOf(modelObj.id)});
        }

        public void setChecked(ShoppingListItem item, boolean checked){
            ContentValues values = new ContentValues();
            values.put("Checked", checked);
            database.getWritableDatabase().update("ShoppingList", values, "ID = ?",
                    new String[]{String.valueOf(item.id)});
        }

        @Override
        public Collection<ShoppingListItem> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM ShoppingList");
            ArrayList<ShoppingListItem> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long id = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("ID"));
                    long ingredientID = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("IngredientID"));
                    String unit = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Unit"));
                    int checked = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("Checked"));
                    ShoppingListItem item = new ShoppingListItem();
                    item.id = id;
                    item.ingredient = RecipeManager.getInstance().getIngredientByID(ingredientID);
                    item.unit = Unit.valueOf(unit);
                    item.checked = checked != 0;
                    list.add(item);
                } while (reader.cursor.moveToNext());
            }
            reader.close();
            return list;
        }
    }

}
