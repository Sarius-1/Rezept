package com.example.meinkochbuch.io;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import androidx.annotation.Nullable;

public class DatabaseHelper extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "recipes";

    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 1);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        String delimiter = "\n";

        //Units
        db.execSQL(String.join(delimiter,
                "CREATE TABLE Unit (",
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                "Name TEXT NOT NULL)"
        ));

        //Ingredient
        db.execSQL(String.join(delimiter,
                "CREATE TABLE Ingredient (",
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                "Name TEXT NOT NULL)"
        ));

        //Recipe
        db.execSQL(String.join(delimiter,
                "CREATE TABLE Recipe (",
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                "Name TEXT NOT NULL,",
                "ProcessingTime INTEGER,",
                "Portions INTEGER,",
                "Rating INTEGER)"
        ));

        //RecipeIngredients
        db.execSQL(String.join(delimiter,
                "CREATE TABLE RecipeIngredients (",
                "IngredientID INTEGER,",
                "RecipeID INTEGER,",
                "Amount INTEGER,",
                "UnitID INTEGER,",
                "PRIMARY KEY (IngredientID, RecipeID),",
                "FOREIGN KEY (IngredientID) REFERENCES Ingredient(ID) ON DELETE CASCADE,",
                "FOREIGN KEY (RecipeID) REFERENCES Recipe(ID) ON DELETE CASCADE,",
                "FOREIGN KEY (UnitID) REFERENCES Unit(ID))"
        ));

        //Shopping list
        db.execSQL(String.join(delimiter,
                "CREATE TABLE ShoppingList (",
                "ID INTEGER PRIMARY KEY AUTOINCREMENT,",
                "IngredientID INTEGER,",
                "UnitID INTEGER,",
                "checked BIT DEFAULT 0,",
                "FOREIGN KEY (IngredientID) REFERENCES Ingredient(ID),",
                "FOREIGN KEY (UnitID) REFERENCES Unit(ID))"
        ));
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }



    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {}
}
