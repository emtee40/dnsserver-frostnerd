package com.frostnerd.dnsserver.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;

import com.frostnerd.dnsserver.database.entities.main.DNSServerSetting;
import com.frostnerd.dnsserver.database.entities.server.DNSEntry;
import com.frostnerd.dnsserver.database.entities.server.DNSQuery;
import com.frostnerd.dnsserver.database.entities.server.UpstreamResponse;
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
public final class ServerDatabaseHelper extends com.frostnerd.utils.database.DatabaseHelper {
    private static final int DATABASE_VERSION = 1;
    private static final Set<Class<? extends Entity>> entities = new HashSet<>();
    static{
        entities.add(DNSQuery.class);
        entities.add(DNSEntry.class);
        entities.add(UpstreamResponse.class);
    }

    public ServerDatabaseHelper(Context context, DNSServerSetting settings){
        super(context, settings.getName(), DATABASE_VERSION, entities);
    }

    @Override
    public void onBeforeCreate(SQLiteDatabase db) {

    }

    @Override
    public void onAfterCreate(SQLiteDatabase db) {

    }

    @Override
    public void onBeforeUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }

    @Override
    public void onAfterUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

    }
}
