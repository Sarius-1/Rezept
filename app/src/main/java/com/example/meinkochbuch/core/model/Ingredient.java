package com.example.meinkochbuch.core.model;

import android.database.sqlite.SQLiteDatabase;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.example.meinkochbuch.io.DatabaseHelper;
import com.example.meinkochbuch.io.SQLModel;

import java.util.ArrayList;
import java.util.Collection;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * Main component behind the recipes. Unique text describes the ingredient itself.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PACKAGE)
public class Ingredient {

    long id;
    String name;

    @Override
    public boolean equals(@Nullable Object obj) {
        if(obj == null || obj.getClass() != getClass())return false;
        Ingredient ingredient = (Ingredient) obj;
        return id == ingredient.id && name.equals(ingredient.name);
    }

    @NonNull
    @Override
    public String toString() {
        return "Ingredient{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    static class SQLIngredient extends SQLModel<Ingredient> {

        SQLIngredient(DatabaseHelper database) {
            super(database);
        }

        @Override
        public String buildCreateStatement() {
            return String.join(DatabaseHelper.CREATION_DELIMITER,
                    "CREATE TABLE Ingredient (",
                    "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                    "Name TEXT NOT NULL)"
            );
        }

        @Override
        public void save(Ingredient ingredient){
            DatabaseHelper.DatabaseWriter writer = database.write("Ingredient");
            writer.values.put("Name", ingredient.name);
            ingredient.id = writer.closeGetID();
        }

        @Override
        public void delete(Ingredient modelObj) {
            SQLiteDatabase db = database.getWritableDatabase();
            db.execSQL("DELETE FROM Ingredient WHERE ID = ?", new Object[]{String.valueOf(modelObj.id)});
        }

        @Override
        public Collection<Ingredient> loadAll() {
            DatabaseHelper.DatabaseReader reader = database.read("SELECT * FROM Ingredient ORDER BY ID ASC");
            ArrayList<Ingredient> list = new ArrayList<>(reader.cursor.getCount());
            if (reader.cursor.moveToFirst()) {
                do {
                    long id = reader.cursor.getInt(reader.cursor.getColumnIndexOrThrow("ID"));
                    String name = reader.cursor.getString(reader.cursor.getColumnIndexOrThrow("Name"));
                    Ingredient ingredient = new Ingredient();
                    ingredient.id = id;
                    ingredient.name = name;
                    list.add(ingredient);
                } while (reader.cursor.moveToNext());
            }
            reader.close();
            return list;
        }

    }

}
