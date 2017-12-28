package com.frostnerd.dnsserver.util;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public class DatabaseHelper extends SQLiteOpenHelper {
    private static final String DATABASE_NAME = "data";
    private static final int DATABASE_VERSION = 1;
    private SQLiteDatabase currentDB;

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        currentDB = db;
        db.execSQL("CREATE TABLE IF NOT EXISTS DNSRules(Domain TEXT NOT NULL, IPv6 BOOL DEFAULT 0, Target TEXT NOT NULL, Wildcard BOOL DEFAULT 0, PRIMARY KEY(Domain, IPv6))");

        currentDB = null;
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        currentDB = db;

        currentDB = null;
    }

    @Override
    public void onConfigure(SQLiteDatabase db) {
        super.onConfigure(db);
    }

    @Override
    public SQLiteDatabase getWritableDatabase() {
        if(currentDB != null){
            return currentDB;
        }
        return super.getWritableDatabase();
    }

    @Override
    public SQLiteDatabase getReadableDatabase() {
        if(currentDB != null){
            return currentDB;
        }
        return super.getReadableDatabase();
    }
}
