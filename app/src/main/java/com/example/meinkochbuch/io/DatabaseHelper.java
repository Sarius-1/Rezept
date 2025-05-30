package com.example.meinkochbuch.io;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import androidx.annotation.Nullable;

import java.util.LinkedList;

public class DatabaseHelper extends SQLiteOpenHelper {

    public static final String CREATION_DELIMITER = " ";
    private static final String DATABASE_NAME = "recipes.db";

    private LinkedList<SQLModel<?>> tableManagers = new LinkedList<>();
    public DatabaseHelper(@Nullable Context context) {
        super(context, DATABASE_NAME, null, 4);
    }

    public void addTableManager(SQLModel<?> model){
        tableManagers.add(model);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.i("DatabaseHelper", "Creating Database");
        for(SQLModel<?> init : tableManagers){
            db.execSQL(init.buildCreateStatement());
        }
        tableManagers = null;

    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
        db.setForeignKeyConstraintsEnabled(true);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        Log.i("Database", "Upgrading DB from v"+oldVersion+" to v"
                +newVersion+" and dropping everything");
        db.beginTransaction();
        try {
            Cursor cursor = db.rawQuery("SELECT name FROM sqlite_master WHERE type='table' AND name NOT LIKE 'sqlite_%'", null);
            while (cursor.moveToNext()) {
                String tableName = cursor.getString(0);
                db.delete(tableName, null, null);
            }
            cursor.close();
            db.setTransactionSuccessful();
        } finally {
            db.endTransaction();
            Log.i("Database", "Dropping done!");
        }
    }

    // -- Database Interaction --

    public DatabaseReader read(String query, String... params){
        return new DatabaseReader(query, params);
    }

    public DatabaseWriter write(String tableName){
        return new DatabaseWriter(tableName);
    }


    public class DatabaseWriter implements AutoCloseable{

        public SQLiteDatabase db;
        public final ContentValues values = new ContentValues();
        private final String table;

        private DatabaseWriter(String table){
            this.table = table;
            this.db = getWritableDatabase();
        }

        @Override
        public void close() {
            closeGetID();
        }

        public long closeGetID(){
            long id = -1;
            if(!values.isEmpty())id = db.insert(table, null, values);
            return id;
        }

    }

    public class DatabaseReader implements AutoCloseable{

        public SQLiteDatabase db;
        public Cursor cursor;
        private DatabaseReader(String query, String... params){
            this.db = getReadableDatabase();
            this.cursor = db.rawQuery(query,params);
        }

        @Override
        public void close() {
            cursor.close();
        }
    }

}
