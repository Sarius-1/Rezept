package com.example.meinkochbuch.core.model;

import android.content.ContentValues;

import androidx.annotation.NonNull;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Represents the model for an item in the shopping list.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class ShoppingListItem {

    long id;
    double amount;
    Ingredient ingredient;
    Unit unit;
    boolean checked;

    @NonNull
    @Override
    public String toString() {
        return "ShoppingListItem{" +
                "id=" + id +
                ", ingredient=" + ingredient +
                ", unit=" + unit +
                ", checked=" + checked +
                '}';
    }
    public void setChecked(boolean checked) {
        this.checked = checked;
        RecipeManager.getInstance().setShoppingListItemChecked(this, checked);
    }

    static class SQLShoppingListItem extends SQLModel<ShoppingListItem> {
        SQLShoppingListItem(DatabaseHelper database) {
            super(database);
        }

        @Override
        protected String tableName() {
            return "ShoppingList";
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE "+tableName+" (",
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "IngredientID INTEGER,",
                    "Amount REAL,",
                    "Unit TEXT,",
                    "Checked INTEGER DEFAULT 0,",
                    "FOREIGN KEY (IngredientID) REFERENCES Ingredient(ID) ON DELETE CASCADE", ")"
            );
        }

        @Override
        public void save(ShoppingListItem modelObj) {
            DatabaseHelper.DatabaseWriter writer = database.write(tableName);
            writer.values.put("IngredientID", modelObj.ingredient.id);
            writer.values.put("Amount", modelObj.amount);
            writer.values.put("Unit", modelObj.unit.name());
            writer.values.put("Checked", modelObj.checked ? 1 : 0);
            modelObj.id = writer.closeGetID();
        }

        @Override
        public void delete(ShoppingListItem modelObj) {
            database.getWritableDatabase().execSQL(
                    "DELETE FROM "+tableName+" WHERE ID = ?", new Object[]{String.valueOf(modelObj.id)});
        }

        public void setChecked(ShoppingListItem item, boolean checked){
            ContentValues values = new ContentValues();
            values.put("Checked", checked);
            database.getWritableDatabase().update(tableName, values, "ID = ?",
                    new String[]{String.valueOf(item.id)});
        }

        public void setAmount(ShoppingListItem item, double amount){
            ContentValues values = new ContentValues();
            values.put("Amount", amount);
            database.getWritableDatabase().update(tableName, values, "ID = ?",
                    new String[]{String.valueOf(item.id)});
        }

        public void setUnit(ShoppingListItem item, Unit unit){
            ContentValues values = new ContentValues();
            values.put("Unit", unit.name());
            database.getWritableDatabase().update(tableName, values, "ID = ?",
                    new String[]{String.valueOf(item.id)});
        }

        @Override
        public Collection<ShoppingListItem> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM "+tableName+" ORDER BY ID ASC");
            ArrayList<ShoppingListItem> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long id = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("ID"));
                    long ingredientID = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("IngredientID"));
                    double amount = reader.cursor.getDouble(reader.cursor.getColumnIndexOrThrow("Amount"));
                    String unit = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Unit"));
                    int checked = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("Checked"));
                    ShoppingListItem item = new ShoppingListItem();
                    item.id = id;
                    item.ingredient = RecipeManager.getInstance().getIngredientByID(ingredientID);
                    item.amount = amount;
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
