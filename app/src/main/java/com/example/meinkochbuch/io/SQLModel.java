package com.example.meinkochbuch.io;

import android.database.sqlite.SQLiteDatabase;

import java.util.Collection;

public abstract class SQLModel<T> {

    protected final DatabaseHelper database;
    public SQLModel(DatabaseHelper database){
        this.database = database;
        database.addTableCreation(buildCreateStatement());
    }

    public abstract String buildCreateStatement();

    public abstract void save(T modelObj);

    public abstract void delete(T modelObj);

    public abstract Collection<T> loadAll();

}
