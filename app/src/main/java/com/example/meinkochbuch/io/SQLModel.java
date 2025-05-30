package com.example.meinkochbuch.io;

import java.util.Collection;

import lombok.Getter;

public abstract class SQLModel<T> {

    protected final DatabaseHelper database;
    @Getter
    protected final String tableName;
    public SQLModel(DatabaseHelper database){
        this.database = database;
        this.tableName = tableName();
        database.addTableManager(this);
    }

    protected abstract String tableName();

    public abstract String buildCreateStatement();

    public abstract void save(T modelObj);

    public abstract void delete(T modelObj);

    public abstract Collection<T> loadAll();

}
