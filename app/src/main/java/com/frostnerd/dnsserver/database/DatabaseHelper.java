package com.frostnerd.dnsserver.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.database.entities.main.IPPortPair;
import com.frostnerd.utils.database.orm.Entity;

import java.util.HashSet;
import java.util.Set;

/**
 * Copyright Daniel Wolf 2017
 * All rights reserved.
 * Code may NOT be used without proper permission, neither in binary nor in source form.
 * All redistributions of this software in source code must retain this copyright header
 * All redistributions of this software in binary form must visibly inform users about usage of this software
 * <p>
 * development@frostnerd.com
 */
public final class DatabaseHelper extends com.frostnerd.utils.database.DatabaseHelper {
    private static final String DATABASE_NAME = "main";
    private static final int DATABASE_VERSION = 1;
    private static final Set<Class<? extends Entity>> entities = new HashSet<>();
    static{
        entities.add(IPPortPair.class);
        entities.add(DNSServerSetting.class);
    }

    public DatabaseHelper(Context context) {
        super(context, DATABASE_NAME, DATABASE_VERSION, entities);
    }

    @Override
    public void onBeforeCreate(SQLiteDatabase db) {

    }

    @Override
    public void onAfterCreate(SQLiteDatabase db) {
        insertTestdata();
    }

    @Override
    public void onBeforeUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onAfterUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    private void insertTestdata(){
        for(int i = 0; i <= 30; i++){
            insert(new DNSServerSetting("SERVER " + i, (int)Math.pow(1.4,i), 1000,
                    IPPortPair.wrap("8.8.8.8", 53),
                    IPPortPair.wrap("8.8.4.4", 53), i%2 == 0, i%2==0, i%2 == 0, i%2==0));
        }

    }
}
